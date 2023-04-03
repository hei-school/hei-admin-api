package java.school.hei.haapi.endpoint.rest.mapper;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class DelayPenaltyMapper {
    public school.hei.haapi.model.DelayPenalty toDelayPenaltyEntity(school.hei.haapi.model.
                                                                            DelayPenaltyChange delayPenaltyChange) {
        return school.hei.haapi.model.DelayPenalty.builder()
                .interestPercent(delayPenaltyChange.getInterestPercent())
                .interestTimeRate(delayPenaltyChange.getInterestTimeRate())
                .graceDelay(delayPenaltyChange.getGraceDelay())
                .applicabilityDelayAfterGrace(delayPenaltyChange.getApplicabilityDelayAfterGrace())
                .creationDatetime(LocalDateTime.now())
                .build();
    }
}