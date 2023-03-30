package school.hei.haapi.service;

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
import school.hei.haapi.model.validator.PaymentValidator;
import school.hei.haapi.repository.PaymentRepository;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.PAID;

@Service
@AllArgsConstructor
public class PaymentService {

  private final FeeService feeService;
  private final PaymentRepository paymentRepository;
  private final PaymentValidator paymentValidator;
  private final InterestHistoryService interestHistoryService;

  public List<Payment> getByStudentIdAndFeeId(
      String studentId, String feeId, PageFromOne page, BoundedPageSize pageSize) {
    Pageable pageable = PageRequest.of(
        page.getValue() - 1,
        pageSize.getValue(),
        Sort.by(DESC, "creationDatetime"));
    return paymentRepository.getByStudentIdAndFeeId(studentId, feeId, pageable);
  }

  public List<Payment> getAllPaymentByStudentIdAndFeeId(
          String studentId, String feeId) {
    return paymentRepository.getAllPaymentByStudentIdAndFeeId(studentId, feeId);
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

  public int computeTotalAmount(school.hei.haapi.model.Fee currentFee){
    return currentFee.getTotalAmount()+interestHistoryService.getInterestAmount(currentFee.getId());
  }
  public int computeRemainingAmountV2(school.hei.haapi.model.Fee currentFee){
    int sumPayment = 0;
    List<Payment> payments = getAllPaymentByStudentIdAndFeeId(currentFee.getStudent().getId(),currentFee.getId());
    for (Payment p:payments) {
      sumPayment = sumPayment + p.getAmount();
    }
    return computeTotalAmount(currentFee)-sumPayment;
  }
}
