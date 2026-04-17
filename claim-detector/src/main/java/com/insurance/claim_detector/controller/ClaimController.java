package com.insurance.claim_detector.controller;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RestController;

import com.insurance.claim_detector.model.InsuranceClaim;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class ClaimController {

    @Autowired
    private KafkaTemplate<String, InsuranceClaim> kafkaTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostMapping("/submitClaim")
    public String postSubmitClaim(@RequestBody InsuranceClaim claim) {

        kafkaTemplate.send("insurance-claims", claim.getClaimId(), claim);

        return "Claim #" + claim.getClaimId() + "summited successfully";
    }

    @GetMapping("/test-rabbit")
    public String TestRabbitMQ() {
        rabbitTemplate.convertAndSend("q.claim-email", "HELLO");
        return "sent";
    }

}
