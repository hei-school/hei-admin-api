package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateDelayPenaltyChange;
import school.hei.haapi.endpoint.rest.model.CreateFee;
import school.hei.haapi.endpoint.rest.model.DelayPenalty;
import school.hei.haapi.endpoint.rest.model.Fee;
import school.hei.haapi.endpoint.rest.validator.CreateFeeValidator;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.model.validator.DelayPenaltyChangeValidator;
import school.hei.haapi.service.DelayPenaltyService;
import school.hei.haapi.service.UserService;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toUnmodifiableList;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.UNPAID;

@Component
@AllArgsConstructor
public class DelayPenaltyMapper {
    private final DelayPenaltyService delayPenaltyService;
    private final DelayPenaltyChangeValidator delayPenaltyChangeValidator;

    public school.hei.haapi.model.DelayPenalty toDomain(CreateDelayPenaltyChange delayPenalty) {
        delayPenaltyChangeValidator.accept(delayPenalty);
        school.hei.haapi.model.DelayPenalty currentDelayPenalty = delayPenaltyService.getCurrentDelayPenalty();

        return school.hei.haapi.model.DelayPenalty.builder()
                .id(currentDelayPenalty.getId())
                .graceDelay(
                        delayPenalty.getGraceDelay() == null ? 0 : delayPenalty.getGraceDelay()
                )
                .interestTimeRate(toDomainDelayPenaltyInterestTimeRateEnum(
                        delayPenalty.getInterestTimeRate() == null ?
                                CreateDelayPenaltyChange.InterestTimeRateEnum.DAILY :
                                delayPenalty.getInterestTimeRate())
                )
                .interestPercent(
                        delayPenalty.getInterestPercent() == null ? 0 : delayPenalty.getInterestPercent()
                )
                .applicabilityDelayAfterGrace(
                        delayPenalty.getApplicabilityDelayAfterGrace() == null ?
                                0 : delayPenalty.getApplicabilityDelayAfterGrace()
                )
                .creationDatetime(Instant.now())
                .build();
    }

    public DelayPenalty toRest(school.hei.haapi.model.DelayPenalty delayPenalty) {
        return new DelayPenalty()
                .creationDatetime(delayPenalty.getCreationDatetime())
                .graceDelay(delayPenalty.getGraceDelay())
                .interestPercent(delayPenalty.getInterestPercent())
                .applicabilityDelayAfterGrace(delayPenalty.getApplicabilityDelayAfterGrace())
                .interestTimeRate(delayPenalty.getInterestTimeRate())
                .id(delayPenalty.getId());
    }

    private DelayPenalty.InterestTimeRateEnum toDomainDelayPenaltyInterestTimeRateEnum(CreateDelayPenaltyChange.InterestTimeRateEnum interestTimeRateEnum) {
        switch (interestTimeRateEnum) {
            case DAILY:
                return DelayPenalty.InterestTimeRateEnum.DAILY;
            default:
                throw new BadRequestException("Unexpected interest time rate: " + interestTimeRateEnum.getValue());
        }
    }
}
