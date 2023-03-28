package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateFee;
import school.hei.haapi.endpoint.rest.model.Fee;
import school.hei.haapi.endpoint.rest.validator.CreateFeeValidator;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.InterestHistory;
import school.hei.haapi.model.Payment;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.service.DelayPenaltyService;
import school.hei.haapi.service.InterestHistoryService;
import school.hei.haapi.service.PaymentService;
import school.hei.haapi.service.UserService;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toUnmodifiableList;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.UNPAID;

@Component
@AllArgsConstructor
public class FeeMapper {

  private final DelayPenaltyService delayPenaltyService;
  private final InterestHistoryService interestHistoryService;
  private final PaymentService paymentService;
  private final UserService userService;
  private final CreateFeeValidator createFeeValidator;

  private int computeTotalAmount(school.hei.haapi.model.Fee currentFee){
    return currentFee.getTotalAmount()+interestHistoryService.getInterestAmount(currentFee.getId());
  }
  private int computeRemainingAmountAmount(school.hei.haapi.model.Fee currentFee){
    int sumPayment = 0;
    List<Payment> payments = paymentService.getAllPaymentByStudentIdAndFeeId(currentFee.getStudent().getId(),currentFee.getId());
    for (Payment p:payments) {
      sumPayment = sumPayment + p.getAmount();
    }
    return computeTotalAmount(currentFee)-sumPayment;
  }

  private int convertInterestTimeRateToDayNumber( school.hei.haapi.endpoint.rest.model.DelayPenalty.InterestTimerateEnum interestTimeRate){
    switch (interestTimeRate) {
      case DAILY:
        return 1;
      default:
        throw new BadRequestException(
                "Unexpected delay Penalty Interest Time rate: " + interestTimeRate.getValue());
    }
  }

  public Fee toRestFee(school.hei.haapi.model.Fee fee) {
    if (!fee.getStatus().equals(Fee.StatusEnum.PAID)){
      ZoneId zone = ZoneId.of("UTC");
      LocalDate today = LocalDate.ofInstant(Instant.now(), zone);
      LocalDate dueTime = LocalDate.ofInstant(fee.getDueDatetime(), zone);
      DelayPenalty generalRule = delayPenaltyService.getAll().get(0);
      if ( Period.between(today, dueTime.plusDays(generalRule.getGraceDelay())).getDays()>=0){
        List<InterestHistory> interestHistories = interestHistoryService.getAllByFeeId(fee.getId());
        if (interestHistories.size()==0){
          //toDO create InterestHistory
          java.util.Date InterestStart = java.util.Date.from(dueTime.plusDays(generalRule.getGraceDelay()+1).atTime(12,0).toInstant(ZoneOffset.UTC));
          java.util.Date InterestEnd = java.util.Date.from(dueTime.plusDays(generalRule.getGraceDelay()+1+ generalRule.getApplicabilityDelayAfterGrace()).atTime(12,0).toInstant(ZoneOffset.UTC));
          InterestHistory toCreate = InterestHistory.builder()
                  .fee(fee)
                  .interestRate(generalRule.getInterestPercent())
                  .interestTimeRate(convertInterestTimeRateToDayNumber(generalRule.getInterestTimeRate()))
                  .interestStart(InterestStart)
                  .interestEnd(InterestEnd)
                  .build();
          interestHistoryService.saveAll(List.of(toCreate));
          interestHistories = interestHistoryService.getAllByFeeId(fee.getId());
        }
        return new Fee()
                .id(fee.getId())
                .studentId(fee.getStudent().getId())
                .status(fee.getStatus())
                .type(fee.getType())
                .totalAmount(computeTotalAmount(fee))
                .remainingAmount(computeRemainingAmountAmount(fee))
                .comment(fee.getComment())
                .creationDatetime(fee.getCreationDatetime())
                .updatedAt(fee.getUpdatedAt())
                .dueDatetime(fee.getDueDatetime());
      }
    }
    return new Fee()
        .id(fee.getId())
        .studentId(fee.getStudent().getId())
        .status(fee.getStatus())
        .type(fee.getType())
        .totalAmount(fee.getTotalAmount())
        .remainingAmount(fee.getRemainingAmount())
        .comment(fee.getComment())
        .creationDatetime(fee.getCreationDatetime())
        .updatedAt(fee.getUpdatedAt())
        .dueDatetime(fee.getDueDatetime());
  }

  private school.hei.haapi.model.Fee toDomainFee(User student, CreateFee createFee) {
    createFeeValidator.accept(createFee);
    if (!student.getRole().equals(User.Role.STUDENT)) {
      throw new BadRequestException("Only students can have fees");
    }
    return school.hei.haapi.model.Fee.builder()
        .student(student)
        .type(toDomainFeeType(Objects.requireNonNull(createFee.getType())))
        .totalAmount(createFee.getTotalAmount())
        .updatedAt(createFee.getCreationDatetime())
        .status(UNPAID)
        .remainingAmount(createFee.getTotalAmount())
        .comment(createFee.getComment())
        .creationDatetime(createFee.getCreationDatetime())
        .dueDatetime(createFee.getDueDatetime())
        .build();
  }

  public List<school.hei.haapi.model.Fee> toDomainFee(String studentId, List<CreateFee> toCreate) {
    User student = userService.getById(studentId);
    if (student == null) {
      throw new NotFoundException("Student.id=" + studentId + " is not found");
    }
    return toCreate
        .stream()
        .map(createFee -> toDomainFee(student, createFee))
        .collect(toUnmodifiableList());
  }

  private Fee.TypeEnum toDomainFeeType(CreateFee.TypeEnum createFeeType) {
    switch (createFeeType) {
      case TUITION:
        return Fee.TypeEnum.TUITION;
      case HARDWARE:
        return Fee.TypeEnum.HARDWARE;
      default:
        throw new BadRequestException("Unexpected feeType: " + createFeeType.getValue());
    }
  }
}
