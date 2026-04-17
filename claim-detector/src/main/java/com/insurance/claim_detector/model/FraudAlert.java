package com.insurance.claim_detector.model;

import java.util.ArrayList;
import java.util.List;

public class FraudAlert {
    
    public String customerName;
    public double totalAmount;
    public int claimCount;
    public List<ClaimDetail> evidence = new ArrayList<>();
    
}
