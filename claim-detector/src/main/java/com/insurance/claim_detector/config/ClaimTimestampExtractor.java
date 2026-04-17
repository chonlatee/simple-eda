package com.insurance.claim_detector.config;

import java.time.ZoneId;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;

import com.insurance.claim_detector.model.InsuranceClaim;

public class ClaimTimestampExtractor implements TimestampExtractor {

    @Override
    public long extract(ConsumerRecord<Object, Object> record, long partitionTime) {
        InsuranceClaim claim = (InsuranceClaim) record.value();

        if (claim != null && claim.getClaimDate() != null) {
            return claim.getClaimDate()
                    .atZone(ZoneId.systemDefault())
                    .toInstant().toEpochMilli();
        }

        return record.timestamp();
    }

}
