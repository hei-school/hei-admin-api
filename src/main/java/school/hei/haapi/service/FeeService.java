package school.hei.haapi.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
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
    return resetPaymentRelatedInfo(feeRepository.getById(id));
  }

  public Fee getByStudentIdAndFeeId(String studentId, String feeId) {
    return resetPaymentRelatedInfo(feeRepository.getByStudentIdAndId(studentId, feeId));
  }

  @Transactional
  public List<Fee> saveAll(List<Fee> fees) {
    feeValidator.accept(fees);
    return resetPaymentRelatedInfo(feeRepository.saveAll(fees));
  }

  // TODO : This request must be cached and refresh every 12 hours
  public List<Fee> getFees(
      PageFromOne page, BoundedPageSize pageSize,
      school.hei.haapi.endpoint.rest.model.Fee.StatusEnum status) {
    List<Fee> allFees = feeRepository.findAll();
    if (status != null) {
      return getFeesByStatus(resetPaymentRelatedInfo(allFees), status, page, pageSize);
    }
    return getFeesByStatus(resetPaymentRelatedInfo(allFees),
        school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.LATE, page, pageSize);
  }

  public List<Fee> getFeesByStudentId(
      String studentId, PageFromOne page, BoundedPageSize pageSize,
      school.hei.haapi.endpoint.rest.model.Fee.StatusEnum status) {
    Pageable pageable = PageRequest.of(
        page.getValue() - 1,
        pageSize.getValue(),
        Sort.by(DESC, "dueDatetime"));
    if (status != null) {
      List<Fee> studentFees = resetPaymentRelatedInfo(feeRepository.getByStudentId(studentId));
      return getFeesByStatus(studentFees, status, page, pageSize);
    }
    return resetPaymentRelatedInfo(feeRepository.getByStudentId(studentId, pageable));
  }

  private List<Fee> getFeesByStatus(
      List<Fee> fees,
      school.hei.haapi.endpoint.rest.model.Fee.StatusEnum status,
      PageFromOne page, BoundedPageSize pageSize) {
    int firstIndex = 0;
    int lastIndex = page.getValue() * pageSize.getValue();
    if (page.getValue() > 1) {
      firstIndex = (page.getValue() - 1) * pageSize.getValue();
    }
    List<Fee> feesByStatus = fees.stream()
        .filter(fee -> fee.getStatus().equals(status))
        .sorted(Comparator.comparing(Fee::getDueDatetime).reversed())
        .collect(Collectors.toUnmodifiableList());
    if (firstIndex >= feesByStatus.size()) {
      return List.of();
    }
    if (lastIndex > feesByStatus.size() - 1) {
      lastIndex = feesByStatus.size();
    }
    return feesByStatus.subList(firstIndex, lastIndex);
  }

  private school.hei.haapi.endpoint.rest.model.Fee.StatusEnum getFeeStatus(Fee fee) {
    if (computeRemainingAmount(fee) == 0) {
      return school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.PAID;
    } else {
      if (Instant.now().isAfter(fee.getDueDatetime())) {
        return school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.LATE;
      }
      return school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.UNPAID;
    }
  }

  private int computeRemainingAmount(Fee fee) {
    List<Payment> payments = fee.getPayments();
    if (payments != null) {
      int amount = payments
          .stream()
          .mapToInt(Payment::getAmount)
          .sum();
      return fee.getTotalAmount() - amount;
    }
    return fee.getTotalAmount();
  }

  private Fee resetPaymentRelatedInfo(Fee initialFee) {
    return Fee.builder()
        .id(initialFee.getId())
        .student(initialFee.getStudent())
        .type(initialFee.getType())
        .remainingAmount(computeRemainingAmount(initialFee))
        .status(getFeeStatus(initialFee))
        .comment(initialFee.getComment())
        .creationDatetime(initialFee.getCreationDatetime())
        .dueDatetime(initialFee.getDueDatetime())
        .payments(initialFee.getPayments())
        .totalAmount(initialFee.getTotalAmount())
        .build();
  }

  private List<Fee> resetPaymentRelatedInfo(List<Fee> fees) {
    return fees.stream()
        .map(this::resetPaymentRelatedInfo)
        .collect(toUnmodifiableList());
  }
}
