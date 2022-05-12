package school.hei.haapi.endpoint.rest.mapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateFee;
import school.hei.haapi.endpoint.rest.model.Fee;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.service.UserService;

@Component
@AllArgsConstructor
public class FeeMapper {

  private final UserService userService;

  public Fee toRestFee(school.hei.haapi.model.Fee fee) {
    Instant truncatedInstant = fee.getCreationDatetime().truncatedTo(ChronoUnit.MILLIS);
    return new Fee()
        .id(fee.getId())
        .studentId(fee.getStudent().getId())
        .status(fee.getStatus())
        .type(fee.getType())
        .totalAmount(fee.getTotalAmount())
        .remainingAmount(fee.getRemainingAmount())
        .comment(fee.getComment())
        .creationDatetime(truncatedInstant)
        .dueDatetime(fee.getDueDatetime());
  }

  private school.hei.haapi.model.Fee toDomainFee(User student, CreateFee createFee) {
    if (createFee.getTotalAmount() == null) {
      throw new BadRequestException("Total amount is mandatory");
    }
    return school.hei.haapi.model.Fee.builder()
        .student(student)
        .type(toDomainFeeType(Objects.requireNonNull(createFee.getType())))
        .totalAmount(createFee.getTotalAmount())
        .comment(createFee.getComment())
        .creationDatetime(createFee.getCreationDatetime())
        .dueDatetime(createFee.getDueDatetime())
        .build();
  }

  public List<school.hei.haapi.model.Fee> toDomainFee(String studentId, List<CreateFee> toCreate) {
    User student = userService.getById(studentId);
    if (student == null) {
      throw new BadRequestException("Student.id=[" + studentId + "] is not found");
    }
    List<school.hei.haapi.model.Fee> fees = new ArrayList<>();
    for (CreateFee c : toCreate) {
      fees.add(toDomainFee(student, c));
    }
    return fees;
  }

  private Fee.TypeEnum toDomainFeeType(CreateFee.TypeEnum createFeeType) {
    String feeType = createFeeType.toString();
    if (Fee.TypeEnum.TUITION.toString().equals(feeType)) {
      return Fee.TypeEnum.TUITION;
    } else if (Fee.TypeEnum.HARDWARE.toString().equals(feeType)) {
      return Fee.TypeEnum.HARDWARE;
    }
    throw new BadRequestException("Unexpected feeType: " + feeType);
  }
}
