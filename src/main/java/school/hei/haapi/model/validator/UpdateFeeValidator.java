package school.hei.haapi.model.validator;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.repository.FeeRepository;

@Component
@AllArgsConstructor
public class UpdateFeeValidator implements Consumer<Fee> {
  public final FeeRepository feeRepository;

  public void accept(List<Fee> fees) {
    fees.forEach(this::accept);
  }

  public void accept(List<school.hei.haapi.endpoint.rest.model.Fee> fees, String studentId) {
    fees.forEach(fee -> accept(fee, studentId));
  }

  public void accept(school.hei.haapi.endpoint.rest.model.Fee fee, String studentId) {
    Set<String> violationMessages = new HashSet<>();
    if (!fee.getStudentId().equals(studentId)) {
      violationMessages.add(
          "Student with Id " + fee.getStudentId() + " must by same as Id in endpoint");
    }
    if (!violationMessages.isEmpty()) {
      String formattedViolationMessages =
          violationMessages.stream().map(String::toString).collect(Collectors.joining(". "));
      throw new BadRequestException(formattedViolationMessages);
    }
  }

  @Override
  public void accept(Fee fee) {
    Set<String> violationMessages = new HashSet<>();
    if (fee.getStudent() == null) {
      violationMessages.add("Student is mandatory");
    }
    if (fee.getDueDatetime() == null) {
      violationMessages.add("Due datetime is mandatory");
    }
    if (fee.getTotalAmount() < 0) {
      violationMessages.add("Total amount must be positive");
    }
    if (fee.getId() == null) {
      violationMessages.add("Id is mandatory");
    } else {
      Optional<Fee> optionalFee = feeRepository.findById(fee.getId());
      if (optionalFee.isEmpty()) {
        violationMessages.add("Fee with id " + fee.getId() + "does not exist");
      } else {
        Fee originalFee = optionalFee.get();
        if (!fee.getTotalAmount().equals(originalFee.getTotalAmount())) {
          violationMessages.add("Can't modify total amount");
        }
        if (!fee.getCreationDatetime().equals(originalFee.getCreationDatetime())) {
          violationMessages.add("Can't modify CreationDatetime");
        }
        if (!fee.getStudent().getId().equals(originalFee.getStudent().getId())) {
          violationMessages.add("Can't modify student");
        }
        if (!fee.getRemainingAmount().equals(originalFee.getRemainingAmount())) {
          violationMessages.add("Can't modify remainingAmount");
        }
        if (!fee.getType().equals(originalFee.getType())) {
          violationMessages.add("Can't modify Type");
        }
        if (!fee.getStatus().equals(originalFee.getStatus())) {
          violationMessages.add("Can't modify Status");
        }
      }
    }
    if (!violationMessages.isEmpty()) {
      String formattedViolationMessages =
          violationMessages.stream().map(String::toString).collect(Collectors.joining(". "));
      throw new BadRequestException(formattedViolationMessages);
    }
  }
}
