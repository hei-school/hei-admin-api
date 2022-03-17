package school.hei.haapi.service;

import java.util.List;
import javax.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.Payment;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.validator.PaymentValidator;
import school.hei.haapi.repository.PaymentRepository;

@Service
@AllArgsConstructor
public class PaymentService {

  private final PaymentRepository paymentRepository;
  private final PaymentValidator paymentValidator;

  public List<Payment> getByFeeId(String feeId) {
    return paymentRepository.getByFeeId(feeId);
  }

  public List<Payment> getByStudentId(String studentId) {
    return paymentRepository.getByStudentId(studentId);
  }

  @Transactional
  public List<Payment> saveAll(String feeId, List<Payment> toCreate) {
    checkStudentPayments(feeId, toCreate);
    return paymentRepository.saveAll(toCreate);
  }

  private void checkStudentPayments(String feeId, Payment toCheck) {
    Fee associatedFee = toCheck.getFee();
    if (!feeId.equals(associatedFee.getId())) {
      throw new BadRequestException(
          "Payment must be associated to fee." + associatedFee.getId()
              + " instead of fee." + feeId);
    }
    if (associatedFee.getRemainingAmount() < toCheck.getAmount()) {
      throw new BadRequestException(
          "Fee remaining amount is " + associatedFee.getRemainingAmount()
              + ". Actual payment amount is " + toCheck.getAmount());
    }
  }

  private void checkStudentPayments(String feeId, List<Payment> payments) {
    paymentValidator.accept(payments);
    payments.forEach(payment -> checkStudentPayments(feeId, payment));
  }
}
