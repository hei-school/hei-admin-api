package school.hei.haapi.model.validator;

import org.springframework.stereotype.Component;
import school.hei.haapi.model.DelayPenalty;

import java.util.function.Consumer;

@Component
public class DelayPenaltyValidator implements Consumer<DelayPenalty> {
    @Override
    public void accept(DelayPenalty delayPenalty) {

    }


}
