package school.hei.haapi.endpoint.rest.mapper;

import static java.util.stream.Collectors.toUnmodifiableList;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.LATE;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.PAID;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.UNPAID;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateFee;
import school.hei.haapi.endpoint.rest.model.Fee;
import school.hei.haapi.endpoint.rest.validator.CreateFeeValidator;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.service.utils.DataFormatterUtils;

@Component
@AllArgsConstructor
public class FeeMapper {

  private final CreateFeeValidator createFeeValidator;

  public Fee toRestFee(school.hei.haapi.model.Fee fee) {
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

  public school.hei.haapi.model.Fee toDomain(Fee fee, User student) {
    Fee.StatusEnum dueDatetimeDependantStatus =
        DataFormatterUtils.isLate(fee.getDueDatetime()) ? LATE : UNPAID;
    return school.hei.haapi.model.Fee.builder()
        .id(fee.getId())
        .student(student)
        .type(fee.getType())
        .totalAmount(fee.getTotalAmount())
        .updatedAt(Instant.now())
        .status(fee.getRemainingAmount() > 0 ? dueDatetimeDependantStatus : PAID)
        .remainingAmount(fee.getRemainingAmount())
        .comment(fee.getComment())
        .creationDatetime(fee.getCreationDatetime())
        .dueDatetime(fee.getDueDatetime())
        .build();
  }

  private school.hei.haapi.model.Fee toDomainFee(User student, CreateFee createFee) {
    createFeeValidator.accept(createFee);
    if (!student.getRole().equals(User.Role.STUDENT)) {
      throw new BadRequestException("Only students can have fees");
    }
    school.hei.haapi.model.Fee fee =
        school.hei.haapi.model.Fee.builder()
            .student(student)
            .type(toDomainFeeType(Objects.requireNonNull(createFee.getType())))
            .totalAmount(createFee.getTotalAmount())
            .updatedAt(createFee.getCreationDatetime())
            .remainingAmount(createFee.getTotalAmount())
            .comment(createFee.getComment())
            .creationDatetime(createFee.getCreationDatetime())
            .dueDatetime(createFee.getDueDatetime())
            .build();

    if (createFee.getDueDatetime() != null) {
      fee.setStatus(DataFormatterUtils.isLate(createFee.getDueDatetime()) ? LATE : UNPAID);
    }
    return fee;
  }

  public List<school.hei.haapi.model.Fee> toDomainFee(User student, List<CreateFee> toCreate) {
    if (student == null) {
      throw new NotFoundException("Student.id=" + student.getId() + " is not found");
    }
    return toCreate.stream()
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
