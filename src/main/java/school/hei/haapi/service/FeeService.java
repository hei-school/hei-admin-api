package school.hei.haapi.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.TypedLateFeeVerified;
import school.hei.haapi.endpoint.event.model.gen.LateFeeVerified;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.model.*;
import school.hei.haapi.model.validator.FeeValidator;
import school.hei.haapi.repository.FeeRepository;
import school.hei.haapi.repository.UserRepository;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.LATE;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.PAID;

@Service
@AllArgsConstructor
@Slf4j
public class FeeService {

  private static final school.hei.haapi.endpoint.rest.model.Fee.StatusEnum DEFAULT_STATUS = LATE;
  private final FeeRepository feeRepository;
  private final FeeValidator feeValidator;

  private final UserRepository userRepository;

  private final EventProducer eventProducer;

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

  public List<Fee> getFees(
      PageFromOne page, BoundedPageSize pageSize,
      school.hei.haapi.endpoint.rest.model.Fee.StatusEnum status) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "dueDatetime"));
    if (status != null) {
      return feeRepository.getFeesByStatus(status, pageable);
    }
    return feeRepository.getFeesByStatus(DEFAULT_STATUS, pageable);
  }

  public static void updateFees(List<Fee> fees) {
    Instant now = Instant.now();


    for (Fee fee : fees) {
      if (fee.getRemainingAmount() > 0 && fee.getDueDatetime().isBefore(now)) {
        int daysLate = (int) ChronoUnit.DAYS.between(fee.getDueDatetime(), now);
        if (daysLate > DelayPenalty.getGraceDelay()) {
          int daysAfterGrace = daysLate - DelayPenalty.getGraceDelay();
          if (daysAfterGrace <= DelayPenalty.getApplicabilityDelayAfterGrace()) {
           int interestAmount = fee.getRemainingAmount() * DelayPenalty.getInterestPercent() * daysAfterGrace / 36500;
            fee.setTotalAmount(fee.getRemainingAmount() + interestAmount);
          }
        }
      }
    }
  }

  public static void updateFeesForOneStudent(List<Fee> fees) {
    Instant now = Instant.now();

    for (Fee fee : fees) {
      if (fee.getRemainingAmount() > 0 && fee.getDueDatetime().isBefore(now)) {
        int daysLate = (int) ChronoUnit.DAYS.between(fee.getDueDatetime(), now);
        if (daysLate > DelayPenalty.getGraceDelayForOneStd()) {
          int daysAfterGrace = daysLate - DelayPenalty.getGraceDelayForOneStd();
          if (daysAfterGrace <= DelayPenalty.getApplicabilityDelayAfterGrace()) {
            int interestAmount = fee.getRemainingAmount() * DelayPenalty.getInterestPercent() * daysAfterGrace / 36500;
            fee.setTotalAmount(fee.getRemainingAmount() + interestAmount);
          }
        }
      }
    }
  }

  public List<Fee> getFeesByStudentId(
      String studentId, PageFromOne page, BoundedPageSize pageSize,
      school.hei.haapi.endpoint.rest.model.Fee.StatusEnum status) {
    Pageable pageable = PageRequest.of(
        page.getValue() - 1,
        pageSize.getValue(),
        Sort.by(DESC, "dueDatetime"));
    List<Fee> studentFees = resetPaymentRelatedInfo(feeRepository.getByStudentId(studentId, pageable));
    User student = userRepository.findById(studentId)
            .orElseThrow(() -> new EntityNotFoundException("Student not found"));
    if (DelayPenalty.getStudent() != null || DelayPenalty.getStudent() == student){
      updateFeesForOneStudent(studentFees);
    }
    updateFees(studentFees);
    if (status != null) {
      return feeRepository.getFeesByStudentIdAndStatus(studentId, status, pageable);
    }
    return feeRepository.getByStudentId(studentId, pageable);
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

  private Fee updateFeeStatus(Fee initialFee) {
    if (initialFee.getRemainingAmount() == 0) {
      initialFee.setStatus(PAID);
    } else if (Instant.now().isAfter(initialFee.getDueDatetime())) {
      initialFee.setStatus(LATE);
    }
    return initialFee;
  }

  @Scheduled(cron = "0 0 * * * *")
  public void updateFeesStatusToLate() {
    List<Fee> unpaidFees = feeRepository.getUnpaidFees();
    unpaidFees.forEach(fee -> {
      updateFeeStatus(fee);
      log.info("Fee with id." + fee.getId() + " is going to be updated from UNPAID to LATE");
    });
    feeRepository.saveAll(unpaidFees);
  }

  private TypedLateFeeVerified toTypedEvent(Fee fee) {
    return new TypedLateFeeVerified(
        LateFeeVerified.builder()
            .type(fee.getType())
            .student(fee.getStudent())
            .comment(fee.getComment())
            .remainingAmount(fee.getRemainingAmount())
            .dueDatetime(fee.getDueDatetime())
            .build()
    );
  }

  /*
   * An email will be sent to user with late fees
   * every morning at 8am (UTC+3)
   * */
  @Scheduled(cron = "0 0 8 * * *")
  public void sendLateFeesEmail() {
    List<Fee> lateFees = feeRepository.getFeesByStatus(LATE);
    lateFees.forEach(
        fee -> {
          eventProducer.accept(List.of(toTypedEvent(fee)));
          log.info("Late Fee with id." + fee.getId() + " is sent to Queue");
        }
    );
  }

}
