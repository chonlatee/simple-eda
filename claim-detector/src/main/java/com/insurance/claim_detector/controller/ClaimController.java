package com.insurance.claim_detector.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RestController;

import com.insurance.claim_detector.model.InsuranceClaim;

import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class ClaimController {

    @Autowired
    private KafkaTemplate<String, InsuranceClaim> kafkaTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/test-rabbit")
    public String TestRabbitMQ() {
        rabbitTemplate.convertAndSend("q.claim-email", "HELLO");
        return "sent";
    }

    @GetMapping("/send-batch-claim")
    public String sendBatchClaim() {
        Random random = new Random();
        String[] normalNames = { "Kik", "Neng", "Tee", "P", "Biw", "Jay", "Nat", "Pert" };
        String[] claimTypes = { "HOME", "CAR", "HEALTH" };
        double[] amountRanges = { 2000, 5000, 10000, 15000, 20000, 25000, 30000, 35000, 40000, 45000, 50000 };

        for (int i = 0; i < 100; i++) {

            InsuranceClaim claim = new InsuranceClaim();

            claim.setClaimId(UUID.randomUUID().toString().substring(0, 8));

            int typeIndex = random.nextInt(claimTypes.length);
            claim.setClaimType(claimTypes[typeIndex]);

            claim.setClaimDate(LocalDateTime.now().minusDays(random.nextInt(50)));
            claim.setCustomerName(normalNames[random.nextInt(normalNames.length)]);
            claim.setAmount(amountRanges[random.nextInt(amountRanges.length)]);

            kafkaTemplate.send("insurance-claims", claim.getCustomerName(), claim);

            System.out.println(
                    "Send claim success with claim id: " + claim.getClaimId() + " customer " + claim.getCustomerName());
        }

        return "Batch claims sent";
    }

}
