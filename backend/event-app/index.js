require('dotenv').config();
const express = require('express');
const users = require('./routes/users');
const events = require('./routes/events');

const app = express();

app.use(express.json());
app.use('/uploads', express.static('uploads'));

// Логирование запросов
app.use((req, res, next) => {
  console.log(`[${new Date().toISOString()}] ${req.method} ${req.url}`);
  next();
});

app.use('/api/users', users);
app.use('/api/events', events);

app.get('/', (req, res) => {
  res.send('Сервер работает!');
});

const PORT = process.env.PORT || 3001;

if (require.main === module) {
  app.listen(PORT, '0.0.0.0', () => {
    console.log(`Сервер запущен на порту ${PORT}`);
  });
}

module.exports = app;