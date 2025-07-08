const express = require('express');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const multer = require('multer');
const path = require('path');
const pool = require('../db');
const authMiddleware = require('../middleware/auth');
const router = express.Router();

// Настройка multer для сохранения файлов
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, 'uploads/');
  },
  filename: (req, file, cb) => {
    const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
    cb(null, file.fieldname + '-' + uniqueSuffix + path.extname(file.originalname));
  }
});
const upload = multer({ storage: storage });

// Регистрация
router.post('/register', async (req, res) => {
  try {
    const { email, password, name, phone } = req.body;

    if (!email || !password || !name) {
      return res.status(400).json({ error: 'Email, пароль и имя обязательны' });
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      return res.status(400).json({ error: 'Невалидный email' });
    }

    if (password.length < 6) {
      return res.status(400).json({ error: 'Пароль должен быть не менее 6 символов' });
    }

    const userExists = await pool.query('SELECT * FROM users WHERE email = $1', [email]);
    if (userExists.rows.length > 0) {
      return res.status(400).json({ error: 'Пользователь с таким email уже существует' });
    }

    const saltRounds = 10;
    const hashedPassword = await bcrypt.hash(password, saltRounds);

    const newUser = await pool.query(
      'INSERT INTO users (email, password, name, phone) VALUES ($1, $2, $3, $4) RETURNING id, email',
      [email, hashedPassword, name, phone || null]
    );

    const token = jwt.sign(
      { userId: newUser.rows[0].id, email: newUser.rows[0].email },
      process.env.JWT_SECRET,
      { expiresIn: '1h' }
    );

    res.status(201).json({
      token,
      userId: newUser.rows[0].id
    });
  } catch (error) {
    console.error('Ошибка при регистрации:', error);
    res.status(500).json({ error: 'Ошибка сервера' });
  }
});

// Авторизация
router.post('/login', async (req, res) => {
  try {
    const { email, password } = req.body;

    if (!email || !password) {
      return res.status(400).json({ error: 'Email и пароль обязательны' });
    }

    const user = await pool.query('SELECT * FROM users WHERE email = $1', [email]);
    if (user.rows.length === 0) {
      return res.status(401).json({ error: 'Неверный email или пароль' });
    }

    const isPasswordValid = await bcrypt.compare(password, user.rows[0].password);
    if (!isPasswordValid) {
      return res.status(401).json({ error: 'Неверный email или пароль' });
    }

    const token = jwt.sign(
      { userId: user.rows[0].id, email: user.rows[0].email },
      process.env.JWT_SECRET,
      { expiresIn: '1h' }
    );

    res.status(200).json({
      token,
      userId: user.rows[0].id
    });
  } catch (error) {
    console.error('Ошибка при авторизации:', error);
    res.status(500).json({ error: 'Ошибка сервера' });
  }
});

// Получение профиля
router.get('/profile', authMiddleware, async (req, res) => {
  try {
    const user = await pool.query('SELECT id, email, name, phone, profile_photo FROM users WHERE id = $1', [req.userId]);
    if (user.rows.length === 0) {
      return res.status(404).json({ error: 'Пользователь не найден' });
    }
    res.json({
      id: user.rows[0].id,
      email: user.rows[0].email,
      name: user.rows[0].name,
      phone: user.rows[0].phone,
      profilePhoto: user.rows[0].profile_photo
    });
  } catch (error) {
    console.error('Ошибка при получении профиля:', error);
    res.status(500).json({ error: 'Не удалось загрузить профиль' });
  }
});

// Обновление профиля
router.put('/profile', authMiddleware, upload.single('photo'), async (req, res) => {
  try {
    const { name, phone } = req.body;
    const profilePhoto = req.file ? `/uploads/${req.file.filename}` : null;

    if (!name) {
      return res.status(400).json({ error: 'Имя обязательно' });
    }

    const updatedUser = await pool.query(
      'UPDATE users SET name = $1, phone = $2, profile_photo = COALESCE($3, profile_photo) WHERE id = $4 RETURNING id, email, name, phone, profile_photo',
      [name, phone || null, profilePhoto, req.userId]
    );

    if (updatedUser.rows.length === 0) {
      return res.status(404).json({ error: 'Пользователь не найден' });
    }

    res.json(updatedUser.rows[0]);
  } catch (error) {
    console.error('Ошибка при обновлении профиля:', error);
    res.status(500).json({ error: 'Не удалось обновить профиль' });
  }
});

// Выход из системы
router.post('/logout', authMiddleware, async (req, res) => {
  try {
    // Для stateless JWT сервер не хранит сессии, поэтому просто возвращаем успешный ответ
    res.status(200).json({ message: 'Выход выполнен успешно' });
  } catch (error) {
    console.error('Ошибка при выходе:', error);
    res.status(500).json({ error: 'Ошибка сервера при выходе' });
  }
});

module.exports = router;