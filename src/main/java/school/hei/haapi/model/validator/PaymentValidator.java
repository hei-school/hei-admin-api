package school.hei.haapi.model.validator;

import java.util.List;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.Payment;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.NotImplementedException;

import static school.hei.haapi.endpoint.rest.model.Payment.TypeEnum.CASH;

@Component
public class PaymentValidator implements Consumer<Payment> {

  public void accept(List<Payment> payments) {
    if (hasMultipleFees(payments)) {
      throw new NotImplementedException("Payments on multiple fees are not yet implemented");
    }
    Fee associatedFee = payments.get(0).getFee();
    int totalAmount = payments
        .stream()
        .mapToInt(Payment::getAmount)
        .sum();
    if (associatedFee.getRemainingAmount() < totalAmount) {
      throw new BadRequestException(
          "Payment amount (" + totalAmount
              + ") exceeds fee remaining amount (" + associatedFee.getRemainingAmount() + ")");
    }
    payments.forEach(this);
  }

  @Override
  public void accept(Payment toCheck) {
    if (toCheck.getAmount() < 0) {
      throw new BadRequestException("Amount must be positive");
    }
    if (!toCheck.getType().equals(CASH) && toCheck.getComment() == null) {
      throw new BadRequestException(
          "Comment is mandatory for " + toCheck.getType().getValue()
      );
    }
  }

  private boolean hasMultipleFees(List<Payment> payments) {
    if (payments.size() <= 1) {
      return false;
    }
    boolean hasMultiple = false;
    for (int i = 0; i < payments.size(); i++) {
      if (hasMultiple) {
        break;
      }
      String actualFeeId = payments.get(i).getFee().getId();
      for (int j = i + 1; j < payments.size(); j++) {
        String nextFeeId = payments.get(j).getFee().getId();
        if (!actualFeeId.equals(nextFeeId)) {
          hasMultiple = true;
          break;
        }
      }
    }
    return hasMultiple;
  }
}
