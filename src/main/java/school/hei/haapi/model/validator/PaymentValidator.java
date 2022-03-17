package school.hei.haapi.model.validator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
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
    Set<String> violationMessages = new HashSet<>();
    if (payment.getFee() == null) {
      violationMessages.add("Fee is mandatory");
    }
    if (payment.getAmount() < 0) {
      violationMessages.add("Amount must be positive");
    }
    if (!violationMessages.isEmpty()) {
      String formattedViolationMessages = violationMessages.stream()
          .map(String::toString)
          .collect(Collectors.joining(". "));
      throw new BadRequestException(formattedViolationMessages);
    }
  }
}
