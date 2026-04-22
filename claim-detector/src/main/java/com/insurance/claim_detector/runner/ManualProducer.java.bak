package com.insurance.claim_detector.runner;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.insurance.claim_detector.model.InsuranceClaim;

@Component
public class ManualProducer implements CommandLineRunner {

    @Autowired
    private KafkaTemplate<String, InsuranceClaim> kafkaTemplate;

    @Override
    public void run(String... args) throws Exception {
        Random random = new Random();
        String[] normalNames = { "Kik", "Neng", "Tee", "P", "Biw", "Jay", "Nat", "Pert" };
        System.out.println("STARTING CLAIM SIMULATOR...");

        for (int i = 0; i < 100; i++) {

            InsuranceClaim claim = new InsuranceClaim();

            claim.setClaimId(UUID.randomUUID().toString().substring(0, 8));
            claim.setClaimType("HOME");
            claim.setClaimDate(LocalDateTime.now().minusDays(random.nextInt(30)));
            claim.setCustomerName(normalNames[random.nextInt(normalNames.length)]);

            double rawAmount = 1000 + (random.nextDouble() * 29000);

            BigDecimal bd = new BigDecimal(rawAmount).setScale(2, RoundingMode.HALF_UP);

            claim.setAmount(bd.doubleValue());

            kafkaTemplate.send("insurance-claims", claim.getCustomerName(), claim);

            System.out.println(
                    "Send claim success with claim id: " + claim.getClaimId() + " customer " + claim.getCustomerName());

            if (i % 10 == 0) {
                System.out.print(".");
                Thread.sleep(20);
            }
        }
        System.out.println("FINISHED: 1000 claims sent. Watch the logs for Alerts!");
    }
}
