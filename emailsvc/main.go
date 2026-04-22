package main

import (
	"encoding/json"
	"fmt"
	"log"
	"time"

	amqp "github.com/rabbitmq/amqp091-go"
)

// Match this to your Java InsuranceClaim fields!
type InsuranceClaim struct {
	ClaimID      string  `json:"claimId"`
	Amount       float64 `json:"amount"`
	ClaimType    string  `json:"claimType"`
	CustomerName string  `json:"customerName"`
	ClaimDate    string  `json:"claimDate"`
}

func main() {
	// 1. Connect to Docker RabbitMQ
	conn, err := amqp.Dial("amqp://guest:guest@localhost:5672/")
	if err != nil {
		log.Fatal("Failed to connect to RabbitMQ:", err)
	}
	defer conn.Close()

	ch, _ := conn.Channel()
	defer ch.Close()

	// 2. Consume from the queue Spring Boot is sending to
	msgs, _ := ch.Consume(
		"q.claim-email", // queue name
		"",              // consumer name
		true,            // auto-ack (true means message is deleted immediately)
		false,
		false,
		false,
		nil,
	)

	fmt.Println("🚀 Go Worker is live. Waiting for small claims...")

	for d := range msgs {
		var claim InsuranceClaim
		err := json.Unmarshal(d.Body, &claim)
		if err != nil {
			log.Printf("error encoding json: %s\n", err.Error())
			continue
		}

		sendEmail(claim)

	}
}

func sendEmail(claim InsuranceClaim) {
	time.Sleep(time.Millisecond * 500)
	log.Printf("CUSTOMER: %s,  AMOUNT: %.2f , send email success", claim.CustomerName, claim.Amount)
}
