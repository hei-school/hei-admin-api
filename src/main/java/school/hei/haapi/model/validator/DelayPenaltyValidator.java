package school.hei.haapi.model.validator;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.exception.BadRequestException;
@Component
public class DelayPenaltyValidator implements Consumer<DelayPenalty> {
    @Override
    public void accept(DelayPenalty delayPenalty) {
        Set<String> violationMessages = new HashSet<>();
            if (delayPenalty.getInterestPercent()<0){
    violationMessages.add("...");
            }if(delayPenalty.getGraceDelay()<0){
                violationMessages.add("...");
        }if(delayPenalty.getApplicabilityDelayAfterGrace()<0){
                violationMessages.add("...");
        }if(!violationMessages.isEmpty()){
                String formatedViolationMessages = violationMessages.stream()
                        .map(String::toString)
                        .collect(Collectors.joining("."));
                throw new BadRequestException(formatedViolationMessages);
        }
    }
}
