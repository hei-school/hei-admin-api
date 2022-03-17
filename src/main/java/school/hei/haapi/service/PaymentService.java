package school.hei.haapi.service;

import java.util.List;
import javax.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.Payment;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.validator.PaymentValidator;
import school.hei.haapi.repository.PaymentRepository;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
@AllArgsConstructor
public class PaymentService {

  private final PaymentRepository paymentRepository;
  private final PaymentValidator paymentValidator;

  public List<Payment> getByStudentIdAndFeeId(
      String studentId, String feeId, PageFromOne page, BoundedPageSize pageSize) {
    Pageable pageable = PageRequest.of(
        page.getValue() - 1,
        pageSize.getValue(),
        Sort.by(DESC, "creationDatetime"));
    return paymentRepository.getByStudentIdAndFeeId(studentId, feeId, pageable);
  }

  @Transactional
  public List<Payment> saveAll(List<Payment> toCreate) {
    checkStudentPayments(toCreate);
    return paymentRepository.saveAll(toCreate);
  }

  private void checkStudentPayments(Payment toCheck) {
    Fee associatedFee = toCheck.getFee();
    if (associatedFee.getRemainingAmount() < toCheck.getAmount()) {
      throw new BadRequestException(
          "Payment amount (" + toCheck.getAmount()
              + ") exceeds fee remaining amount (" + associatedFee.getRemainingAmount() + ")");
    }
  }

  private void checkStudentPayments(List<Payment> payments) {
    paymentValidator.accept(payments);
    payments.forEach(payment -> checkStudentPayments(payment));
  }
}
