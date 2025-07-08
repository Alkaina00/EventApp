const express = require('express');
const router = express.Router();
const pool = require('../db');
const authMiddleware = require('../middleware/auth');

// Получение списка событий
router.get('/', authMiddleware, async (req, res) => {
  try {
    const events = await pool.query('SELECT * FROM events');
    res.json(events.rows);
  } catch (error) {
    console.error('Ошибка при получении событий:', error);
    res.status(500).json({ error: 'Failed to load events' });
  }
});

// Создание события
router.post('/', authMiddleware, async (req, res) => {
  const { title, description, date, location, city } = req.body;

  if (!title || !date || !location || !city) {
    return res.status(400).json({ error: 'Обязательные поля: title, date, location, city' });
  }

  try {
    const newEvent = await pool.query(
      'INSERT INTO events (title, description, event_date, location, city, creator_id, created_at) VALUES ($1, $2, $3, $4, $5, $6, $7) RETURNING *',
      [title, description, new Date(date), location, city, req.userId, new Date()]
    );
    res.status(201).json(newEvent.rows[0]);
  } catch (error) {
    console.error('Ошибка при создании события:', error);
    res.status(500).json({ error: 'Не удалось создать событие' });
  }
});

// Редактирование события
router.put('/:id', authMiddleware, async (req, res) => {
  const { id } = req.params;
  const { title, description, date, location, city } = req.body;

  if (!title || !date || !location || !city) {
    return res.status(400).json({ error: 'Обязательные поля: title, date, location, city' });
  }

  try {
    const event = await pool.query('SELECT * FROM events WHERE id = $1', [id]);
    if (event.rows.length === 0) {
      return res.status(404).json({ error: 'Событие не найдено' });
    }
    if (event.rows[0].creator_id !== req.userId) {
      return res.status(403).json({ error: 'Вы не можете редактировать это событие' });
    }
    if (new Date(event.rows[0].date) < new Date()) {
      return res.status(400).json({ error: 'Нельзя редактировать прошедшие события' });
    }

    const updatedEvent = await pool.query(
      'UPDATE events SET title = $1, description = $2, date = $3, location = $4, city = $5 WHERE id = $6 RETURNING *',
      [title, description, new Date(date), location, city, id]
    );
    res.json(updatedEvent.rows[0]);
  } catch (error) {
    console.error('Ошибка при обновлении события:', error);
    res.status(500).json({ error: 'Не удалось обновить событие' });
  }
});

// Удаление события
router.delete('/:id', authMiddleware, async (req, res) => {
  const { id } = req.params;

  try {
    const event = await pool.query('SELECT * FROM events WHERE id = $1', [id]);
    if (event.rows.length === 0) {
      return res.status(404).json({ error: 'Событие не найдено' });
    }
    if (event.rows[0].creator_id !== req.userId) {
      return res.status(403).json({ error: 'Вы не можете удалить это событие' });
    }
    if (new Date(event.rows[0].date) < new Date()) {
      return res.status(400).json({ error: 'Нельзя удалить прошедшие события' });
    }

    await pool.query('DELETE FROM events WHERE id = $1', [id]);
    res.json({ message: 'Событие удалено' });
  } catch (error) {
    console.error('Ошибка при удалении события:', error);
    res.status(500).json({ error: 'Не удалось удалить событие' });
  }
});

module.exports = router;