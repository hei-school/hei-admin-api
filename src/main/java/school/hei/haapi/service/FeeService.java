package school.hei.haapi.service;

import java.util.List;
import javax.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.validator.FeeValidator;
import school.hei.haapi.repository.FeeRepository;

@Service
@AllArgsConstructor
public class FeeService {

  private final FeeRepository feeRepository;
  private final FeeValidator feeValidator;

  public Fee getByStudentIdAndFeeId(String userId, String feeId) {
    return feeRepository.getByStudentIdAndId(userId, feeId);
  }

  @Transactional
  public List<Fee> saveAll(String studentId, List<Fee> fees) {
    checkStudentFees(studentId, fees);
    for (Fee f : fees) {
      f.setRemainingAmount(computeRemainingAmount(f));
      f.setStatus(getFeeStatus(f));
    }
    return feeRepository.saveAll(fees);
  }

  public List<Fee> getFeesByStudentId(String studentId) {
    return feeRepository.getByStudentId(studentId);
  }

  private int computeRemainingAmount(Fee fee) {
    //TODO : remaining amount is computed from payment status associated to the fee
    return fee.getTotalAmount(); //By default return total amount
  }

  private school.hei.haapi.endpoint.rest.model.Fee.StatusEnum getFeeStatus(Fee fee) {
    //TODO : remaining amount is computed from payment status associated to the fee
    return school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.UNPAID; //By default return UNPAID
  }

  private void checkStudentFees(String studentId, Fee toCheck) {
    if (!studentId.equals(toCheck.getStudentId())) {
      throw new BadRequestException(
          "Fee must be associated to student." + toCheck.getStudentId() + " instead of student."
              + studentId);
    }
  }

  private void checkStudentFees(String studentId, List<Fee> fees) {
    feeValidator.accept(fees);
    fees.forEach(fee -> checkStudentFees(studentId, fee));
  }
}
