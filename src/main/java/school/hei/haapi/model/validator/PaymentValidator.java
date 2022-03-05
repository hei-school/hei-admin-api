package school.hei.haapi.model.validator;

import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.Payment;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.NotImplementedException;

import static school.hei.haapi.endpoint.rest.model.Payment.TypeEnum.CASH;

@Component
@AllArgsConstructor
public class PaymentValidator implements Consumer<Payment> {

    public void accept(List<Payment> payments) {
        if (hasMultipleFees(payments))
            throw new NotImplementedException("Payments on multiple fees are not implemented");
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
        payments.forEach(this::accept);
    }

    @Override
    public void accept(Payment toCheck) {
        if (toCheck.getAmount() < 0) {
            throw new BadRequestException("Amount must be positive");
        }
        if (!toCheck.getType().equals(CASH)) {
            throw new BadRequestException(
                    "Comment is mandatory for " + toCheck.getType().getValue()
            );
        }
    }

    private boolean hasMultipleFees(List<Payment> payments) {
        if (payments.size() <= 1) return false;
        int count = 0;
        for (int i = 0; i < payments.size(); i++) {
            if (count >= 2) break;
            Fee fee = payments.get(i).getFee();
            for (int j = 1; j < payments.size(); j++) {
                if (fee.equals(payments.get(j).getFee())) {
                    count += 1;
                }
            }
        }
        return count >= 2;
    }
}
