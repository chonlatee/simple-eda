const express = require('express');
const http = require('http');
const { Server } = require('socket.io');
const Kafka = require('kafkajs');
const sqlite3 = require('sqlite3').verbose();

const app = express();
const server = http.createServer(app);
const io = new Server(server);


app.get('/', (req, res) => {
  res.sendFile(__dirname + '/index.html');
});



async function runKafka() {
  const kafka = new Kafka.Kafka({
    clientId: 'home-monitoring-viewer',
    brokers: ['localhost:9092']
  });

  const consumer = kafka.consumer({ groupId: 'home-monitoring-group' });
  await consumer.connect();
  await consumer.subscribe({ topic: 'insurance-home-claim-fraud', fromBeginning: false });

  await consumer.run({
    eachMessage: async ({ message }) => {
      const alert = JSON.parse(message.value.toString());
      console.log('Received claim data:', alert);
      io.emit('fraud-alert', alert);      
    }
  })
}

runKafka().catch(console.error);  

server.listen(3333, () => {
  console.log('Server is listening on port 3333');
});
