package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.validator.FeeValidator;
import school.hei.haapi.repository.FeeRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.LATE;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.PAID;

@Service
@AllArgsConstructor
public class FeeService {

  private final FeeRepository feeRepository;
  private final FeeValidator feeValidator;

  public Fee getById(String id) {
    return feeRepository.getById(id);
  }

  public Fee getByStudentIdAndFeeId(String studentId, String feeId) {
    return feeRepository.getByStudentIdAndId(studentId, feeId);
  }

  public Fee checkRemainingAmount(Fee fee) {
    if (fee.getRemainingAmount() == 0) {
      fee.setStatus(PAID);
    }
    return fee;
  }

  @Transactional
  public List<Fee> saveAll(List<Fee> fees) {
    feeValidator.accept(fees);
    List<Fee> checkedFees = fees.stream()
        .map(this::checkRemainingAmount)
        .collect(Collectors.toUnmodifiableList());
    return feeRepository.saveAll(checkedFees);
  }

  // TODO : This request must be cached and refresh every 12 hours
  public List<Fee> getFees(
      PageFromOne page, BoundedPageSize pageSize,
      school.hei.haapi.endpoint.rest.model.Fee.StatusEnum status) {
    Pageable pageable = PageRequest.of(
        page.getValue() - 1,
        pageSize.getValue(),
        Sort.by(DESC, "dueDatetime"));

    if (status != null) {
      return feeRepository.getFeesByStatus(status, pageable);
    }
    return feeRepository.getFeesByStatus(LATE, pageable);
  }

  public List<Fee> getFeesByStudentId(
      String studentId, PageFromOne page, BoundedPageSize pageSize,
      school.hei.haapi.endpoint.rest.model.Fee.StatusEnum status) {
    Pageable pageable = PageRequest.of(
        page.getValue() - 1,
        pageSize.getValue(),
        Sort.by(DESC, "dueDatetime"));
    if (status != null) {
      return feeRepository.getFeesByStudentIdAndStatus(studentId, status, pageable);
    }
    return feeRepository.getByStudentId(studentId, pageable);
  }

}
