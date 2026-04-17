package com.insurance.claim_detector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@SpringBootApplication
@EnableKafkaStreams
public class ClaimDetectorApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClaimDetectorApplication.class, args);
	}

}
