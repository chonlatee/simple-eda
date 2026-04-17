package com.insurance.claim_detector.model;

public class ClaimDetail {
    public String claimId;
    public double amount;
    public String claimDate;

    public ClaimDetail(String id, double amount, String date) {
        this.claimId = id;
        this.amount = amount;
        this.claimDate = date;
    }
}

    
