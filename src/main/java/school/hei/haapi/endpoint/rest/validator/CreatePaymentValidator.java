package school.hei.haapi.endpoint.rest.validator;

import java.util.function.Consumer;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreatePayment;
import school.hei.haapi.model.exception.BadRequestException;

@Component
public class CreatePaymentValidator implements Consumer<CreatePayment> {
  @Override public void accept(CreatePayment createPayment) {
    if (createPayment.getAmount() == null) {
      throw new BadRequestException("Amount is mandatory");
    }
  }
}