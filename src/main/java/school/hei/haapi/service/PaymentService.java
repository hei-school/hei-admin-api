package school.hei.haapi.service;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.PAID;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.Payment;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.model.validator.PaymentValidator;
import school.hei.haapi.repository.PaymentRepository;

@Service
@AllArgsConstructor
public class PaymentService {

  private final FeeService feeService;
  private final PaymentRepository paymentRepository;
  private final PaymentValidator paymentValidator;

  public Payment deleteById(String paymentId) {
    Payment payment = getById(paymentId);
    paymentRepository.deleteById(paymentId);
    return payment;
  }

  public Payment getById(String paymentId) {
    return paymentRepository
        .findById(paymentId)
        .orElseThrow(() -> new NotFoundException("Payment with id: " + paymentId + " not found"));
  }

  public List<Payment> getByStudentIdAndFeeId(
      String studentId, String feeId, PageFromOne page, BoundedPageSize pageSize) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "creationDatetime"));
    return paymentRepository.getByStudentIdAndFeeId(studentId, feeId, pageable);
  }

  public void computeRemainingAmount(String feeId, int amount) {
    Fee associatedFee = feeService.getById(feeId);
    associatedFee.setRemainingAmount(associatedFee.getRemainingAmount() - amount);
    if (associatedFee.getRemainingAmount() == 0) {
      associatedFee.setStatus(PAID);
    }
  }

  @Transactional
  public List<Payment> saveAll(List<Payment> toCreate) {
    paymentValidator.accept(toCreate);
    toCreate.forEach(
        payment -> computeRemainingAmount(payment.getFee().getId(), payment.getAmount()));
    return paymentRepository.saveAll(toCreate);
  }
}
