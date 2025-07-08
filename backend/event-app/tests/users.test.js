const request = require('supertest');
const app = require('../index');
const pool = require('../db');

beforeAll(async () => {
  await pool.query('TRUNCATE users RESTART IDENTITY CASCADE');
});

afterAll(async () => {
  await pool.end();
});

describe('POST /api/users/register', () => {
  it('должен зарегистрировать пользователя', async () => {
    const res = await request(app)
      .post('/api/users/register')
      .send({
        email: 'test2@example.com',
        password: '123456',
        name: 'Тестовый Пользователь',
        phone: '+1234567890',
      });
    expect(res.statusCode).toBe(201);
    expect(res.body.message).toBe('Пользователь успешно зарегистрирован');
  });

  it('должен вернуть ошибку при невалидном email', async () => {
    const res = await request(app)
      .post('/api/users/register')
      .send({
        email: 'invalid-email',
        password: '123456',
        name: 'Тестовый Пользователь',
      });
    expect(res.statusCode).toBe(400);
    expect(res.body.error).toBe('Невалидный email');
  });
});

describe('POST /api/users/login', () => {
  it('должен авторизовать пользователя и вернуть JWT', async () => {
    await request(app)
      .post('/api/users/register')
      .send({
        email: 'login@example.com',
        password: '123456',
        name: 'Логин Тест',
        phone: '+1234567890',
      });

    const res = await request(app)
      .post('/api/users/login')
      .send({
        email: 'login@example.com',
        password: '123456',
      });
    expect(res.statusCode).toBe(200);
    expect(res.body.message).toBe('Авторизация успешна');
    expect(res.body.token).toBeDefined();
    expect(res.body.user).toHaveProperty('id');
    expect(res.body.user.email).toBe('login@example.com');
  });

  it('должен вернуть ошибку при неверном пароле', async () => {
    const res = await request(app)
      .post('/api/users/login')
      .send({
        email: 'login@example.com',
        password: 'wrongpassword',
      });
    expect(res.statusCode).toBe(401);
    expect(res.body.error).toBe('Неверный email или пароль');
  });
});

describe('GET /api/users/profile', () => {
  it('должен вернуть профиль пользователя', async () => {
    const registerRes = await request(app)
      .post('/api/users/register')
      .send({
        email: 'profile@example.com',
        password: '123456',
        name: 'Profile Test',
        phone: '+1234567890',
      });

    const loginRes = await request(app)
      .post('/api/users/login')
      .send({
        email: 'profile@example.com',
        password: '123456',
      });

    const token = loginRes.body.token;

    const res = await request(app)
      .get('/api/users/profile')
      .set('Authorization', `Bearer ${token}`);

    expect(res.statusCode).toBe(200);
    expect(res.body.email).toBe('profile@example.com');
    expect(res.body.name).toBe('Profile Test');
    expect(res.body.phone).toBe('+1234567890');
  });

  it('должен вернуть ошибку при отсутствии токена', async () => {
    const res = await request(app).get('/api/users/profile');
    expect(res.statusCode).toBe(401);
    expect(res.body.error).toBe('No token provided');
  });
});