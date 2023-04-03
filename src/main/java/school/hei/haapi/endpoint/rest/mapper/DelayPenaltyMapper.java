package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateDelayPenaltyChange;
import school.hei.haapi.endpoint.rest.model.DelayPenalty;
import school.hei.haapi.endpoint.rest.validator.CreateDelayPenaltyValidator;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.service.DelayPenaltyService;

import java.time.Instant;

@Component
public class DelayPenaltyMapper {
    DelayPenaltyService service;
    CreateDelayPenaltyValidator validator;
    public DelayPenalty toRest(school.hei.haapi.model.DelayPenalty domain) {
        return new DelayPenalty()
                .id(domain.getId())
         public DelayPenalty toRest(school.hei.haapi.model.DelayPenalty domain) {
                .applicabilityDelayAfterGrace(domain.getApplicabilityDelayAfterGrace())
                    .creationDatetime(domain.getCreationDatetime());
        }
        public school.hei.haapi.model.DelayPenalty toDomain(CreateDelayPenaltyChange createDelayPenaltyChange) {
            validator.accept(createDelayPenaltyChange);
            school.hei.haapi.model.DelayPenalty lastDelayPenalty = service.getLastUpdated();
            return school.hei.haapi.model.DelayPenalty.builder()
                    .id(lastDelayPenalty.getId())
                    .interestPercent(createDelayPenaltyChange.getInterestPercent())
                    .creationDatetime(lastDelayPenalty.getCreationDatetime())
                    .lastUpdateDate(Instant.now())
                    .interestTimeRate(toDomainDelayInterest(createDelayPenaltyChange.getInterestTimerate()))
                    .graceDelay(createDelayPenaltyChange.getGraceDelay())
                    .applicabilityDelayAfterGrace(createDelayPenaltyChange.getApplicabilityDelayAfterGrace())
                    .build();
        }

        private DelayPenalty.InterestTimerateEnum toDomainDelayInterest(CreateDelayPenaltyChange.InterestTimerateEnum interestTimerate) {
            switch (interestTimerate) {
                case DAILY:
                    return school.hei.haapi.endpoint.rest.model.DelayPenalty.InterestTimerateEnum.DAILY;
                default:
                    throw new BadRequestException(
                            "Unexpected delayPenaltyInterestTimeRate: " + interestTimerate.getValue());
            }
        }

    }
