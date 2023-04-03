package school.hei.haapi.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
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
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.FeesHistory;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.model.validator.FeeValidator;
import school.hei.haapi.repository.FeeHistoryRepository;
import school.hei.haapi.repository.FeeRepository;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.LATE;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.PAID;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.UNPAID;

@Service
@AllArgsConstructor
@Slf4j
public class FeeService {

  private static final school.hei.haapi.endpoint.rest.model.Fee.StatusEnum DEFAULT_STATUS = LATE;
  private final FeeRepository feeRepository;
  private final FeeValidator feeValidator;
  private final UserService userService;
  private final DelayPenaltyService delayPenaltyService;

  private final FeeHistoryRepository feeHistoryRepository;
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

  private Fee updateFeeStatus(Fee initialFee) {
    if (initialFee.getRemainingAmount() == 0) {
      initialFee.setStatus(PAID);
    } else if (Instant.now().isAfter(initialFee.getDueDatetime())) {
      initialFee.setStatus(LATE);
    }
    return initialFee;
  }
  @Transactional
  public double addInterest(Fee fee){
    User user = userService.getById(fee.getStudent().getId());
    DelayPenalty delayPenalty = delayPenaltyService.getAll();
    Optional<FeesHistory> feesHistory = Optional.ofNullable(feeHistoryRepository.getByStudentId(fee.getStudent().getId()));
    boolean there = feesHistory.isPresent();
    FeesHistory feesHistories;
    if(there) {
      feesHistories = feeHistoryRepository.getByStudentId(fee.getStudent().getId());
    }
    else{
      feesHistories = new FeesHistory();
    }
    int i = 0;
    int amount = 0;
    double temp = fee.getTotalAmount();
    Instant graceDelay =
      feesHistories.getGraceStudentDelay() > 0 ? fee.getDueDatetime().plus(
              feesHistories.getGraceStudentDelay(),
              ChronoUnit.DAYS
      ) : fee.getDueDatetime().plus(
              delayPenalty.getGraceDelay(),
              ChronoUnit.DAYS
      );

    int delay =((delayPenalty.getApplicabilityDelayAfterGrace()) - 1);
    if(Instant.now().isAfter(graceDelay) && fee.getStatus() == LATE){
      while(i < delay){
        feesHistories.setStudent(user);
        feesHistories.setFee_total(temp);
        feesHistories.setPercentage(delayPenalty.getInterestPercent());
          amount += ((fee.getTotalAmount() * delayPenalty.getInterestPercent()) / 100) * i ;
          temp += amount;
          delay --;
        i++;
      }
    }
    else {
      feesHistories.setFee_total(temp);
      feesHistories.setPercentage(delayPenalty.getInterestPercent());
      feesHistories.setPaid(true);
      fee.setTotalAmount(feesHistories.getFee_total().intValue());
      fee.setRemainingAmount(0);
    }
    feesHistories.setStudent(fee.getStudent());
    feeHistoryRepository.save(feesHistories);
    return temp;
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
