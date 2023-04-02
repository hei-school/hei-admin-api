package school.hei.haapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.InterestTimeRate;
import school.hei.haapi.repository.DelayPenaltyRepository;


import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class DelayPenaltyService {

    @Autowired
    private DelayPenaltyRepository delayPenaltyRepository;

    public BigDecimal calculatePenalty(String delayPenaltyId, BigDecimal amount, int daysDelayed) {

        // Get the DelayPenalty entity from the repository
        DelayPenalty delayPenalty = delayPenaltyRepository.findById(delayPenaltyId).orElseThrow(() -> new RuntimeException("Delay penalty not found"));


        // Calculate the interest rate based on the specified time rate
        BigDecimal interestRate = delayPenalty.getInterestPercent().divide(BigDecimal.valueOf(100));
        if (delayPenalty.getInterestTimeRate() == InterestTimeRate.DAILY) {
            interestRate = interestRate.divide(BigDecimal.valueOf(365), 10, RoundingMode.HALF_UP);
        } else if (delayPenalty.getInterestTimeRate() == InterestTimeRate.WEEKLY) {
            interestRate = interestRate.divide(BigDecimal.valueOf(52), 10, RoundingMode.HALF_UP);
        } else if (delayPenalty.getInterestTimeRate() == InterestTimeRate.MONTHLY) {
            interestRate = interestRate.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);
        }

        // Calculate the penalty amount using the compound interest formula
        BigDecimal penaltyAmount = amount.multiply(BigDecimal.ONE.add(interestRate).pow(daysDelayed)).setScale(2, RoundingMode.HALF_UP);

        return penaltyAmount;
    }

    public DelayPenalty getDelayPenalty(String delayPenaltyId) {
        return delayPenaltyRepository.findById(delayPenaltyId)
                .orElseThrow(() -> new RuntimeException("Delay penalty not found"));
    }
}

