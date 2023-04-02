package school.hei.haapi.endpoint.rest.mapper;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateFee;
import school.hei.haapi.endpoint.rest.model.DelayPenalty;
import school.hei.haapi.endpoint.rest.model.Fee;
import school.hei.haapi.endpoint.rest.validator.CreateFeeValidator;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.service.UserService;

import static java.util.stream.Collectors.toUnmodifiableList;

@Component
@AllArgsConstructor
public class DelayPenaltyMapper {


    public DelayPenalty toRest(school.hei.haapi.model.DelayPenalty delayPenalty){
        return new DelayPenalty()
                .graceDelay(school.hei.haapi.model.DelayPenalty.getGraceDelay())
                .interestPercent(school.hei.haapi.model.DelayPenalty.getInterestPercent())
                .interestTimerate(school.hei.haapi.model.DelayPenalty.getInterestTimeRate())
                .applicabilityDelayAfterGrace(school.hei.haapi.model.DelayPenalty.getApplicabilityDelayAfterGrace());
    }

}
