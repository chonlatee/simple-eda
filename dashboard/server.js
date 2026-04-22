const express = require('express');
const http = require('http');
const { Server } = require('socket.io');
const Kafka = require('kafkajs');
const sqlite3 = require('sqlite3').verbose();


const app = express();
const server = http.createServer(app);
const io = new Server(server);

const db = new sqlite3.Database('./claims.db');

db.serialize(() => {
  db.run(`CREATE TABLE IF NOT EXISTS claims (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    claimId TEXT,
    customerName TEXT,
    amount REAL,
    claim_date DATETIME,
    create_date DATETIME DEFAULT CURRENT_TIMESTAMP
  )`);
});

app.get('/', (req, res) => {
  res.sendFile(__dirname + '/index.html');
});

app.get('/delete-claims', (req, res) => {
  db.run("DELETE FROM claims", [], (err) => {
    if (err) {
      console.error('Error deleting claims:', err);
      res.status(500).send('Error deleting claims');
    } else {
      console.log('All claims deleted successfully');
      res.send('All claims deleted successfully');
    }
  });
})

io.on('connection', (socket) => {
  
  console.log('A client connected to the dashboard');
  db.all("SELECT customerName, SUM(amount) AS total FROM claims GROUP BY customerName ORDER BY customerName DESC LIMIT 10", [], (err, rows) => {
    if (err) {
      console.error('Error fetching initial claim data:', err);
      return;
    }
    socket.emit('initial-data', rows);
  });
})


const kafka = new Kafka.Kafka({
  clientId: 'dashboard-viewer',
  brokers: ['localhost:9092']
});

const consumer = kafka.consumer({ groupId: 'dashboard-group' });

const runKafka = async () => {
  await consumer.connect();



  await consumer.subscribe({ topic: 'small-claim-amount-topic', fromBeginning: false });


  await consumer.run({
    eachMessage: async ({ topic, partition, message }) => {
      const claimData = JSON.parse(message.value.toString());
      console.log('Received claim data');


      const stmt = db.prepare("INSERT INTO claims (customerName, amount, claim_date) VALUES (?, ?, ?)");
      stmt.run(claimData.customerName, claimData.amount, claimData.claimDate)
      stmt.finalize();

      console.log("Saved claim data to database:", claimData.claimId);

      sendUpdateTotalClaims();

    },
  });
}

function sendUpdateTotalClaims() {
  db.all("SELECT customerName, SUM(amount) AS total FROM claims GROUP BY customerName ORDER BY customerName DESC LIMIT 10", [], (err, rows) => {
    if (err) {
      console.error('Error fetching total claims data:', err);
      return;
    }
    io.emit('claim-update', rows);
  });
}

runKafka().catch(console.error);

server.listen(3000, () => {
  console.log('Dashboard server is running on http://localhost:3000');
});
