package school.hei.haapi.endpoint.rest.validator;


import java.util.function.Consumer;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateFee;
import school.hei.haapi.model.exception.BadRequestException;

@Component
public class CreateFeeValidator implements Consumer<CreateFee> {
  @Override public void accept(CreateFee createFee) {
    if (createFee.getTotalAmount() == null) {
      throw new BadRequestException("Total amount is mandatory");
    }
  }
}
