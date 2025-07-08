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
    res.status(500).json({ error: 'Не удалось загрузить события' });
  }
});

// Поиск событий
router.get('/search', authMiddleware, async (req, res) => {
  try {
    const { query, status = 'PUBLISHED' } = req.query;
    const searchQuery = `%${query || ''}%`;
    const events = await pool.query(
      'SELECT * FROM events WHERE status = $1 AND (title ILIKE $2 OR description ILIKE $2 OR city ILIKE $2 OR location ILIKE $2)',
      [status, searchQuery]
    );
    res.json(events.rows);
  } catch (error) {
    console.error('Ошибка при поиске событий:', error);
    res.status(500).json({ error: 'Не удалось выполнить поиск' });
  }
});

// Создание события
router.post('/', authMiddleware, async (req, res) => {
  const { title, description, date, location, city, status } = req.body;

  if (!title || !date || !location || !city || !status) {
    return res.status(400).json({ error: 'Обязательные поля: title, date, location, city, status' });
  }

  try {
    const newEvent = await pool.query(
      'INSERT INTO events (title, description, event_date, location, city, creator_id, created_at, status) VALUES ($1, $2, $3, $4, $5, $6, $7, $8) RETURNING *',
      [title, description, new Date(date), location, city, req.userId, new Date(), status]
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
  const { title, description, date, location, city, status } = req.body;

  if (!title || !date || !location || !city || !status) {
    return res.status(400).json({ error: 'Обязательные поля: title, date, location, city, status' });
  }

  try {
    const event = await pool.query('SELECT * FROM events WHERE id = $1', [id]);
    if (event.rows.length === 0) {
      return res.status(404).json({ error: 'Событие не найдено' });
    }
    if (event.rows[0].creator_id !== req.userId) {
      return res.status(403).json({ error: 'Вы не можете редактировать это событие' });
    }
    if (new Date(event.rows[0].event_date) < new Date()) {
      return res.status(400).json({ error: 'Нельзя редактировать прошедшие события' });
    }

    const updatedEvent = await pool.query(
      'UPDATE events SET title = $1, description = $2, event_date = $3, location = $4, city = $5, status = $6 WHERE id = $7 RETURNING *',
      [title, description, new Date(date), location, city, status, id]
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