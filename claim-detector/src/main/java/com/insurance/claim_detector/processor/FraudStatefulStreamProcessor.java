package com.insurance.claim_detector.processor;

import java.time.Duration;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JacksonJsonSerde;
import com.insurance.claim_detector.model.ClaimAggregation;
import com.insurance.claim_detector.model.ClaimDetail;
import com.insurance.claim_detector.model.FraudAlert;
import com.insurance.claim_detector.model.InsuranceClaim;

@Configuration
public class FraudStatefulStreamProcessor {

    @Bean
    public KStream<String, FraudAlert> kStream(StreamsBuilder streamsBuilder) {

        JacksonJsonSerde<InsuranceClaim> claimSerde = new JacksonJsonSerde<>(InsuranceClaim.class);
        JacksonJsonSerde<ClaimAggregation> aggSerde = new JacksonJsonSerde<>(ClaimAggregation.class);
        JacksonJsonSerde<FraudAlert> alertSerde = new JacksonJsonSerde<>(FraudAlert.class);

        KStream<String, InsuranceClaim> claimStream = streamsBuilder.stream("large-claim-amount-topic",
                Consumed.with(Serdes.String(), claimSerde));

        KStream<String, FraudAlert> alertStream = claimStream
                .filter((id, claim) -> claim.getClaimType().equals("HOME") && claim.getAmount() > 20000)
                .groupByKey(Grouped.with(Serdes.String(), claimSerde))
                // .windowedBy(SlidingWindows.ofTimeDifferenceWithNoGrace(Duration.ofDays(30)))
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofDays(30)))
                .aggregate(
                        ClaimAggregation::new,
                        (key, newClaim, agg) -> {
                            agg.add(newClaim);
                            return agg;
                        },
                        Materialized.with(Serdes.String(), aggSerde))
                .toStream()
                .filter((wk, agg) -> agg.claims.size() >= 3)
                .map((wk, agg) -> {
                    FraudAlert alert = new FraudAlert();
                    alert.customerName = wk.key().toString();

                    for (int i = 0; i < agg.claims.size(); i++) {

                        InsuranceClaim c = agg.claims.get(i);

                        ClaimDetail detail = new ClaimDetail(c.getClaimId(), c.getAmount(),
                                c.getClaimDate().toString());

                        alert.evidence.add(detail);
                    }

                    alert.claimCount = alert.evidence.size();

                    double sum = 0;
                    for (InsuranceClaim c : agg.claims) {
                        sum += c.getAmount();
                    }
                    alert.totalAmount = sum;

                    return KeyValue.pair(wk.key().toString(), alert);
                })
                .peek((k, v) -> System.out.println("ALERT GENERATED: " + v.customerName));

        alertStream.to("insurance-home-claim-fraud", Produced.with(Serdes.String(), alertSerde));

        return alertStream;

    }

}
