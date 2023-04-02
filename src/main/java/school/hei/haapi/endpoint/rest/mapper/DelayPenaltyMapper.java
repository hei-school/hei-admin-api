package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;

@Components
public class DelayPenaltyMapper {
    public DelayPenalty toDelayPenaltyEntity(DelayPenaltyChange delayPenaltyChange) {
        return DelayPenalty.builder()
                .interestPercent(delayPenaltyChange.getInterestPercent())
                .interestTimeRate(delayPenaltyChange.getInterestTimeRate())
                .graceDelay(delayPenaltyChange.getGraceDelay())
                .applicabilityDelayAfterGrace(delayPenaltyChange.getDelayPenaltyChange())
                .creationDatetime(LocalDateTime.now())
                .build();
    }
}