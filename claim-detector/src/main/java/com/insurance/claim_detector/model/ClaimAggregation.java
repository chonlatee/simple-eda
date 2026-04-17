package com.insurance.claim_detector.model;

import java.util.ArrayList;
import java.util.List;

public class ClaimAggregation {
   public List<InsuranceClaim> claims = new ArrayList<>();
    
   

   public ClaimAggregation() {};
   
   public void add(InsuranceClaim claim) {
       boolean exists = claims.stream().anyMatch(c -> c.getClaimId().equals(claim.getClaimId()));
       if (!exists) {
            claims.add(claim);
       }
   }
}
