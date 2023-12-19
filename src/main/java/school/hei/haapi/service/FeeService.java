package school.hei.haapi.service;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.LATE;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.PAID;
import static school.hei.haapi.service.utils.InstantUtils.getNextDueDateTime;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.gen.LateFeeVerified;
import school.hei.haapi.endpoint.rest.model.CreateFeeOption;
import school.hei.haapi.model.*;
import school.hei.haapi.model.validator.FeeValidator;
import school.hei.haapi.model.validator.UpdateFeeValidator;
import school.hei.haapi.repository.FeeRepository;

@Service
@AllArgsConstructor
@Slf4j
public class FeeService {

  private static final school.hei.haapi.endpoint.rest.model.Fee.StatusEnum DEFAULT_STATUS = LATE;
  private final FeeRepository feeRepository;
  private final FeeValidator feeValidator;
  private final UpdateFeeValidator updateFeeValidator;
  private final EventProducer eventProducer;
  private final FeeTypeService feeTypeService;
  private final UserService userService;

  public Fee getById(String id) {
    return updateFeeStatus(feeRepository.getById(id));
  }

  public Fee getByStudentIdAndFeeId(String studentId, String feeId) {
    return updateFeeStatus(feeRepository.getByStudentIdAndId(studentId, feeId));
  }

  @Transactional
  public List<Fee> saveAll(List<Fee> fees) {
    feeValidator.accept(fees);
    return feeRepository.saveAll(fees);
  }
  @Transactional
  public List<Fee> saveAll(CreateFeeOption creatFeeOption) {
    FeeTypeEntity feeTypeEntity = feeTypeService.findById(creatFeeOption.getFeeTypeId());
    User student = userService.getById(creatFeeOption.getStudentId());
    List<Fee> fees = new ArrayList<>();
    Instant actualTimestamp = creatFeeOption.getFirstDueDatetime();
    Integer delay = 0;
    for (FeeTypeComponentEntity feeTypeComponent:feeTypeEntity.getFeeTypeComponentEntities()) {
        for (int i = 0; i < feeTypeComponent.getMonthsNumber(); i++) {
          Integer toPayMonthly = feeTypeComponent.getTotalAmount() / feeTypeComponent.getMonthsNumber();
          Fee fee = Fee.builder()
                  .type(school.hei.haapi.endpoint.rest.model.Fee.TypeEnum.fromValue(feeTypeComponent.getType().getValue()))
                  .creationDatetime(actualTimestamp)
                  .student(student)
                  .comment(feeTypeComponent.getName())
                  .dueDatetime(getNextDueDateTime(creatFeeOption.getFirstDueDatetime(), creatFeeOption.getIsInEndOfMonth(),delay))
                  .updatedAt(actualTimestamp)
                  .totalAmount(toPayMonthly)
                  .remainingAmount(toPayMonthly)
                  .status(school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.UNPAID)
                  .build();
          fees.add(fee);
          delay++;
      }
    }
    return saveAll(fees);
  }

  @Transactional
  public List<Fee> updateAll(List<Fee> fees, String studentId) {
    updateFeeValidator.accept(fees);
    return feeRepository.saveAll(fees);
  }

  public List<Fee> getFees(
      PageFromOne page,
      BoundedPageSize pageSize,
      school.hei.haapi.endpoint.rest.model.Fee.StatusEnum status) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "dueDatetime"));
    if (status != null) {
      return feeRepository.getFeesByStatus(status, pageable);
    }
    return feeRepository.getFeesByStatus(DEFAULT_STATUS, pageable);
  }

  public List<Fee> getFeesByStudentId(
      String studentId,
      PageFromOne page,
      BoundedPageSize pageSize,
      school.hei.haapi.endpoint.rest.model.Fee.StatusEnum status) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "dueDatetime"));
    if (status != null) {
      return feeRepository.getFeesByStudentIdAndStatus(studentId, status, pageable);
    }
    return feeRepository.getByStudentId(studentId, pageable);
  }

  private Fee updateFeeStatus(Fee initialFee) {
    if (initialFee.getRemainingAmount() == 0) {
      initialFee.setStatus(PAID);
    } else if (Instant.now().isAfter(initialFee.getDueDatetime())) {
      initialFee.setStatus(LATE);
    }
    return initialFee;
  }

  public void updateFeesStatusToLate() {
    List<Fee> unpaidFees = feeRepository.getUnpaidFees();
    unpaidFees.forEach(
        fee -> {
          updateFeeStatus(fee);
          log.info("Fee with id." + fee.getId() + " is going to be updated from UNPAID to LATE");
        });
    feeRepository.saveAll(unpaidFees);
  }

  private LateFeeVerified toLateFeeEvent(Fee fee) {
    return LateFeeVerified.builder()
        .type(fee.getType())
        .student(fee.getStudent())
        .comment(fee.getComment())
        .remainingAmount(fee.getRemainingAmount())
        .dueDatetime(fee.getDueDatetime())
        .build();
  }

  public void sendLateFeesEmail() {
    List<Fee> lateFees = feeRepository.getFeesByStatus(LATE);
    lateFees.forEach(
        fee -> {
          eventProducer.accept(List.of(toLateFeeEvent(fee)));
          log.info("Late Fee with id." + fee.getId() + " is sent to Queue");
        });
  }
}
