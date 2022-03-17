package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.Payment;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.validator.FeeValidator;
import school.hei.haapi.repository.FeeRepository;

import static java.util.stream.Collectors.toUnmodifiableList;

@Service
@AllArgsConstructor
public class FeeService {

  private final FeeRepository feeRepository;
  private final FeeValidator feeValidator;

  public Fee getById(String id) {
    return refreshFees(feeRepository.getById(id));
  }

  public Fee getByStudentIdAndFeeId(String studentId, String feeId) {
    return refreshFees(feeRepository.getByStudentIdAndId(studentId, feeId));
  }

  public List<Fee> saveAll(String studentId, List<Fee> fees) {
    checkStudentFees(studentId, fees);
    return refreshFees(feeRepository.saveAll(fees));
  }

  public List<Fee> getFeesByStudentId(String studentId) {
    return refreshFees(feeRepository.getByStudentId(studentId));
  }

  private school.hei.haapi.endpoint.rest.model.Fee.StatusEnum getFeeStatus(Fee fee) {
    if (fee.getRemainingAmount() == 0) {
      return school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.PAID;
    }
    return school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.UNPAID;
  }

  private int computeRemainingAmount(Fee fee) {
    List<Payment> payments = fee.getPaymentList();
    int amount = 0;
    if (payments != null) {
      for (Payment payment : payments) {
        amount += payment.getAmount();
      }
    }
    return fee.getTotalAmount() - amount;
  }

  private Fee refreshFees(Fee fee) {
    fee.setRemainingAmount(computeRemainingAmount(fee));
    fee.setStatus(getFeeStatus(fee));
    return fee;
  }

  private List<Fee> refreshFees(List<Fee> fees) {
    return fees.stream()
        .map(this::refreshFees)
        .collect(toUnmodifiableList());
  }

  private void checkStudentFees(String studentId, Fee toCheck) {
    if (!studentId.equals(toCheck.getStudent().getId())) {
      throw new BadRequestException(
          "Fee must be associated to student." + toCheck.getStudent().getId()
              + " instead of student." + studentId);
    }
  }

  private void checkStudentFees(String studentId, List<Fee> fees) {
    feeValidator.accept(fees);
    fees.forEach(fee -> checkStudentFees(studentId, fee));
  }
}
