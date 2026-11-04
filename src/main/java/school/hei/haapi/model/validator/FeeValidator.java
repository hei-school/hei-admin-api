package school.hei.haapi.model.validator;

import static school.hei.haapi.model.User.Status.DISABLED;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.exception.BadRequestException;

@Component
public class FeeValidator implements Consumer<Fee> {

  public void accept(List<Fee> fees) {
    fees.forEach(this);
  }

  @Override
  public void accept(Fee fee) {
    Set<String> violationMessages = new HashSet<>();
    if (fee.getStudent() == null) {
      violationMessages.add("Student is mandatory");
    } else if (DISABLED.equals(fee.getStudent().getStatus())) {
      violationMessages.add(
          "Cannot create fee for student ref=" + fee.getStudent().getRef() + " who left school");
    }
    if (fee.getDueDatetime() == null) {
      violationMessages.add("Due datetime is mandatory");
    }
    if (fee.getTotalAmount() < 0) {
      violationMessages.add("Total amount must be positive");
    }
    if (!violationMessages.isEmpty()) {
      String formattedViolationMessages =
          violationMessages.stream().map(String::toString).collect(Collectors.joining(". "));
      throw new BadRequestException(formattedViolationMessages);
    }
  }
}
