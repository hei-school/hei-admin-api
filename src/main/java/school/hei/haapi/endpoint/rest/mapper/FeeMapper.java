package school.hei.haapi.endpoint.rest.mapper;

import static java.util.stream.Collectors.toUnmodifiableList;
import static school.hei.haapi.endpoint.rest.mapper.FileInfoMapper.ONE_DAY_DURATION_AS_LONG;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.LATE;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.PAID;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.UNPAID;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.HARDWARE;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.REMEDIAL_COSTS;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.STUDENT_INSURANCE;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.TUITION;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.*;
import school.hei.haapi.endpoint.rest.validator.CreateFeeValidator;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.service.LetterService;
import school.hei.haapi.service.aws.FileService;
import school.hei.haapi.service.utils.DataFormatterUtils;

@Component
@AllArgsConstructor
public class FeeMapper {

  private final CreateFeeValidator createFeeValidator;
  private PaymentMapper paymentMapper;
  private final MpbsMapper mpbsMapper;
  private final LetterService letterService;
  private final FileService fileService;

  public ModelFee toRestModelFee(school.hei.haapi.model.Fee fee) {
    var studentFee = fee.getStudent();
    return new ModelFee()
        .studentId(studentFee.getId())
        .studentRef(studentFee.getRef())
        .status(fee.getStatus())
        .type(fee.getType())
        .totalAmount(fee.getTotalAmount())
        .remainingAmount(fee.getRemainingAmount())
        .comment(fee.getComment())
        .creationDatetime(fee.getCreationDatetime())
        .updatedAt(fee.getUpdatedAt())
        .dueDatetime(fee.getDueDatetime())
        .payments(
            fee.getPayments().stream()
                .map(paymentMapper::toRestPayment)
                .collect(toUnmodifiableList()));
  }

  public Fee toRestFee(school.hei.haapi.model.Fee fee) {
    Mpbs feeMpbs = fee.getMpbs() != null ? mpbsMapper.toRest(fee.getMpbs()) : null;
    var studentFee = fee.getStudent();
    var letter = letterService.getByFeeId(fee.getId());

    return new Fee()
        .id(fee.getId())
        .studentId(studentFee.getId())
        .studentRef(studentFee.getRef())
        .status(fee.getStatus())
        .type(fee.getType())
        .totalAmount(fee.getTotalAmount())
        .remainingAmount(fee.getRemainingAmount())
        .comment(fee.getComment())
        .mpbs(feeMpbs)
        .creationDatetime(fee.getCreationDatetime())
        .updatedAt(fee.getUpdatedAt())
        .dueDatetime(fee.getDueDatetime())
        .letter(letter == null ? null : toLetterFee(letter));
  }

  public FeeLetter toLetterFee(school.hei.haapi.model.Letter letter) {
    String fileUrl =
        letter.getFilePath() == null
            ? null
            : fileService.getPresignedUrl(letter.getFilePath(), ONE_DAY_DURATION_AS_LONG);
    return new FeeLetter()
        .id(letter.getId())
        .ref(letter.getRef())
        .status(letter.getStatus())
        .approvalDatetime(letter.getApprovalDatetime())
        .creationDatetime(letter.getCreationDatetime())
        .fileUrl(fileUrl);
  }

  public school.hei.haapi.model.Fee toDomain(Fee fee, User student) {
    FeeStatusEnum dueDatetimeDependantStatus =
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
            .type(createFee.getType())
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

  private FeeTypeEnum toDomainFeeType(FeeTypeEnum createFeeType) {
    switch (createFeeType) {
      case TUITION:
        return TUITION;
      case HARDWARE:
        return HARDWARE;
      case REMEDIAL_COSTS:
        return REMEDIAL_COSTS;
      case STUDENT_INSURANCE:
        return STUDENT_INSURANCE;
      default:
        throw new BadRequestException("Unexpected feeType: " + createFeeType.getValue());
    }
  }
}
