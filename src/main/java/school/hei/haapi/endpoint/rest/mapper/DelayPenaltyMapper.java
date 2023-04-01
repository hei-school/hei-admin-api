package school.hei.haapi.endpoint.rest.mapper;

import java.time.Instant;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateDelayPenaltyChange;
import school.hei.haapi.endpoint.rest.model.DelayPenalty;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.repository.DelayPenaltyRepository;

@Component
@AllArgsConstructor
public class DelayPenaltyMapper {
    private final DelayPenaltyRepository repository;
    public school.hei.haapi.model.DelayPenalty toDomain(CreateDelayPenaltyChange rest) {
        school.hei.haapi.model.DelayPenalty persisted = repository.findAll().get(0);
        return persisted.toBuilder()
            .interestPercent(rest.getInterestPercent())
            .interestTimerate(toDomain(rest.getInterestTimerate()))
            .graceDelay(rest.getGraceDelay())
            .applicabilityDelayAfterGrace(rest.getApplicabilityDelayAfterGrace())
            .creationDatetime(Instant.now())
            .build();
    }

    public DelayPenalty toRest(school.hei.haapi.model.DelayPenalty domain) {
        return new DelayPenalty()
            .id(domain.getId())
            .interestPercent(domain.getInterestPercent())
            .interestTimerate(domain.getInterestTimerate())
            .graceDelay(domain.getGraceDelay())
            .applicabilityDelayAfterGrace(domain.getApplicabilityDelayAfterGrace())
            .creationDatetime(domain.getCreationDatetime());
    }

    private DelayPenalty.InterestTimerateEnum toDomain(
        CreateDelayPenaltyChange.InterestTimerateEnum rest) {
        if (rest == CreateDelayPenaltyChange.InterestTimerateEnum.DAILY) {
            return DelayPenalty.InterestTimerateEnum.DAILY;
        }
        throw new BadRequestException("Unexpected feeType: " + rest.getValue());
    }
}
