const { Pool } = require('pg');

const pool = new Pool({
  user: 'postgres',
  host: 'localhost',
  database: 'event_app',
  password: '135135', // Замени на свой пароль PostgreSQL
  port: 5432,
});

module.exports = pool;