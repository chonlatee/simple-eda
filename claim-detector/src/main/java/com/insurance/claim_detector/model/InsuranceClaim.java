package com.insurance.claim_detector.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceClaim {
    
    private String claimId;
    private String customerName;
    private String claimType;
    private double amount;
    private LocalDateTime claimDate;
}
