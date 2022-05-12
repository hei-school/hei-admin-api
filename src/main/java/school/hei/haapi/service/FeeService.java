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
import school.hei.haapi.model.validator.FeeValidator;
import school.hei.haapi.repository.FeeRepository;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.springframework.data.domain.Sort.Direction.DESC;

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

  @Transactional
  public List<Fee> saveAll(List<Fee> fees) {
    feeValidator.accept(fees);
    return refreshFees(feeRepository.saveAll(fees));
  }

  public List<Fee> getFeesByStudentId(
      String studentId, PageFromOne page, BoundedPageSize pageSize) {
    Pageable pageable = PageRequest.of(
        page.getValue() - 1,
        pageSize.getValue(),
        Sort.by(DESC, "dueDatetime"));
    return refreshFees(feeRepository.getByStudentId(studentId, pageable));
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
}
