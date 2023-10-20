package school.hei.haapi.endpoint.rest.mapper;

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

import static java.util.stream.Collectors.toUnmodifiableList;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.UNPAID;

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
