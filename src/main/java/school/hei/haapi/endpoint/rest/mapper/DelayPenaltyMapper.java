package school.hei.haapi.endpoint.rest.mapper;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateDelayPenaltyChange;
import school.hei.haapi.endpoint.rest.model.DelayPenalty;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.repository.DelayPenaltyRepository;
import school.hei.haapi.repository.FeeRepository;

@Component
@AllArgsConstructor
public class DelayPenaltyMapper {
    private final DelayPenaltyRepository repository;
    private final FeeRepository feeRepository;
    public school.hei.haapi.model.DelayPenalty toDomain(CreateDelayPenaltyChange rest) {
        /*Set isUpToDate of each fee to false when updating delay penalty conf
        * It will automatically set to true the following day by the scheduler. */
        List<Fee> fees = feeRepository.findAll().stream()
            .map(fee -> fee.toBuilder().isUpToDate(false).build())
            .collect(Collectors.toUnmodifiableList());
        feeRepository.saveAll(fees);

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
