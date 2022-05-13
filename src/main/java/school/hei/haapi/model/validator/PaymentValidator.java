package school.hei.haapi.model.validator;

import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.Payment;
import school.hei.haapi.model.exception.BadRequestException;

@Component
@AllArgsConstructor
public class PaymentValidator implements Consumer<Payment> {

  public void accept(List<Payment> payments) {
    payments.forEach(this::accept);
  }

  @Override public void accept(Payment payment) {
    if (payment.getAmount() < 0) {
      throw new BadRequestException("Amount must be positive");
    }
  }
}
