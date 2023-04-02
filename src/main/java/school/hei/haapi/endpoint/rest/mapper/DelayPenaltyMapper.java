package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.DelayPenalty;
import school.hei.haapi.endpoint.rest.validator.UpdateDelayPenaltyValidator;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.service.DelayPenaltyService;

import java.time.Instant;

@Component
@AllArgsConstructor
public class DelayPenaltyMapper {
    private final DelayPenaltyService service;
    private final UpdateDelayPenaltyValidator validator;
    public DelayPenalty toRest(school.hei.haapi.model.DelayPenalty domain) {
        return new DelayPenalty()
                .id(domain.getId())
                .interestPercent(domain.getInterestPercent())
                .interestTimerate(domain.getInterestTimeRate())
                .graceDelay(domain.getGraceDelay())
                .applicabilityDelayAfterGrace(domain.getApplicabilityDelayAfterGrace())
                .creationDatetime(domain.getCreationDatetime());
    }
    public school.hei.haapi.model.DelayPenalty toDomain(DelayPenalty delayPenalty) {
        validator.accept(delayPenalty);
        school.hei.haapi.model.DelayPenalty lastDelayPenalty = service.getLastUpdated();
        return school.hei.haapi.model.DelayPenalty.builder()
                .id(lastDelayPenalty.getId())
                .interestPercent(delayPenalty.getInterestPercent())
                .creationDatetime(lastDelayPenalty.getCreationDatetime())
                .lastUpdateDate(Instant.now())
                .interestTimeRate(toDomainDelayInterest(delayPenalty.getInterestTimerate()))
                .graceDelay(delayPenalty.getGraceDelay())
                .applicabilityDelayAfterGrace(delayPenalty.getApplicabilityDelayAfterGrace())
                .build();
    }

    private DelayPenalty.InterestTimerateEnum toDomainDelayInterest(DelayPenalty.InterestTimerateEnum interestTimerate) {
        switch (interestTimerate) {
            case DAILY:
                return school.hei.haapi.endpoint.rest.model.DelayPenalty.InterestTimerateEnum.DAILY;
            default:
                throw new BadRequestException(
                        "Unexpected delayPenaltyInterestTimeRate: " + interestTimerate.getValue());
        }
    }

}
