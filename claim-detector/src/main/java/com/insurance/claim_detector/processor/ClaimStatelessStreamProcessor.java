package com.insurance.claim_detector.processor;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JacksonJsonSerde;

import com.insurance.claim_detector.model.InsuranceClaim;

@Configuration
public class ClaimStatelessStreamProcessor {

        private final RabbitTemplate rabbitTemplate;

        public ClaimStatelessStreamProcessor(RabbitTemplate rabbitTemplate) {
                this.rabbitTemplate = rabbitTemplate;
        }

        @Bean
        public KStream<String, InsuranceClaim> processClaim(StreamsBuilder streamsBuilder) {

                JacksonJsonSerde<InsuranceClaim> claimSerde = new JacksonJsonSerde<>(InsuranceClaim.class);

                KStream<String, InsuranceClaim> sourceStream = streamsBuilder.stream("insurance-claims",
                                Consumed.with(Serdes.String(), claimSerde));

                sourceStream.split()
                                .branch((key, claim) -> claim.getAmount() >= 20000,
                                                Branched.withConsumer(ks -> ks.to("large-claim-amount-topic",
                                                                Produced.with(Serdes.String(), claimSerde))))

                                .branch((key, claim) -> claim.getAmount() < 20000,
                                                Branched.withConsumer(ks -> {
                                                        ks.to("small-claim-amount-topic",
                                                                        Produced.with(Serdes.String(), claimSerde));

                                                        ks.peek((key, claim) -> {
                                                                try {
                                                                        System.out.println("send message to queue");
                                                                        rabbitTemplate.convertAndSend("q.claim-email",
                                                                                        claim);
                                                                } catch (Exception e) {
                                                                        System.out.println(
                                                                                        "FAILED to send to RabbitMQ: "
                                                                                                        + e.getMessage());
                                                                }
                                                        });
                                                }))

                                .defaultBranch(Branched
                                                .withConsumer(
                                                                (ks -> ks.to("unknow-claim-amount-toic", Produced
                                                                                .with(Serdes.String(), claimSerde)))));

                return sourceStream;

        }

}
