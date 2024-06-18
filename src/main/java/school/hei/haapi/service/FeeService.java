package school.hei.haapi.service;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.LATE;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.PAID;

import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.gen.LateFeeVerified;
import school.hei.haapi.endpoint.event.gen.UnpaidFeesReminder;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.validator.FeeValidator;
import school.hei.haapi.model.validator.UpdateFeeValidator;
import school.hei.haapi.repository.FeeRepository;

@Service
@AllArgsConstructor
@Slf4j
public class FeeService {

  private static final school.hei.haapi.endpoint.rest.model.FeeStatusEnum DEFAULT_STATUS = LATE;
  private final FeeRepository feeRepository;
  private final FeeValidator feeValidator;
  private final UpdateFeeValidator updateFeeValidator;
  private final EventProducer eventProducer;

  public Fee deleteFeeByStudentIdAndFeeId(String studentId, String feeId) {
    Fee deletedFee = getByStudentIdAndFeeId(studentId, feeId);
    feeRepository.deleteById(feeId);
    return deletedFee;
  }

  public Fee getById(String id) {
    var loggedFee = updateFeeStatus(feeRepository.getById(id));
    log.info("fee: ------------########## {}", loggedFee.toString());
    log.info("now: ---------------#########" + Instant.now());
    return loggedFee;
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
  public List<Fee> updateAll(List<Fee> fees, String studentId) {
    updateFeeValidator.accept(fees);
    return feeRepository.saveAll(fees);
  }

  public List<Fee> getFees(
      PageFromOne page,
      BoundedPageSize pageSize,
      school.hei.haapi.endpoint.rest.model.FeeStatusEnum status) {
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
      school.hei.haapi.endpoint.rest.model.FeeStatusEnum status) {
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
    return feeRepository.save(initialFee);
  }

  @Transactional
  public List<Fee> updateFeesStatusToLate() {
    Instant now = Instant.now();
    List<Fee> unpaidFees = feeRepository.getUnpaidFees(now);
    var lateFees = new ArrayList<Fee>();
    unpaidFees.forEach(
        fee -> {
          var modifiedFee = updateFeeStatus(fee);
          log.info(
              "Fee "
                  + modifiedFee.describe()
                  + "with id."
                  + fee.getId()
                  + " is going to be updated from UNPAID to "
                  + fee.getStatus());
          /*if (PAID.equals(modifiedFee.getStatus())) {
            paidFees.add(modifiedFee);
          } else*/
          if (LATE.equals(modifiedFee.getStatus())) {
            lateFees.add(modifiedFee);
          }
        });
    lateFees.forEach(lf -> feeRepository.updateFeeStatusById(LATE, lf.getId()));
    log.info("lateFees = {}", lateFees.stream().map(Fee::describe).toList());
    // paidFees.forEach(lf -> feeRepository.updateFeeStatusById(PAID, lf.getId()));
    return lateFees;
  }

  @Transactional
  public LateFeeVerified toLateFeeEvent(Fee fee) {
    return LateFeeVerified.builder()
        .type(fee.getType())
        .student(LateFeeVerified.FeeUser.from(fee.getStudent()))
        .comment(fee.getComment())
        .remainingAmount(fee.getRemainingAmount())
        .dueDatetime(fee.getDueDatetime())
        .build();
  }

  public UnpaidFeesReminder toUnpaidFeesReminder(Fee fee) {
    return UnpaidFeesReminder.builder()
            .studentEmail(fee.getStudent().getEmail())
            .remainingAmount(fee.getRemainingAmount())
            .id(fee.getId())
            .dueDatetime(fee.getDueDatetime())
            .build();
  }

  @Transactional
  public void sendLateFeesEmail() {
    List<Fee> lateFees = feeRepository.findAllByStatus(LATE);
    log.info("Late fees size: " + lateFees.size());
    lateFees.forEach(
        fee -> {
          eventProducer.accept(List.of(toLateFeeEvent(fee)));
          log.info("Late Fee with id." + fee.getId() + " is sent to Queue");
        });
  }

    public void sendUnpaidFeesEmail () {
      List<Fee> unpaidFees =
              feeRepository.getUnpaidFeesForTheMonthSpecified(
                      Instant.now().atZone(ZoneId.of("UTC+3")).getMonthValue());
      log.info("Unpaid fees size: {}", unpaidFees.size());
      unpaidFees.forEach(
              unpaidFee -> {
                eventProducer.accept(List.of(toUnpaidFeesReminder(unpaidFee)));
                log.info("Unpaid fee with id.{} is sent to Queue", unpaidFee.getId());
              });
    }
  }
