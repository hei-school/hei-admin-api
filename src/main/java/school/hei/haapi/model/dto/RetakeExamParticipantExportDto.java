package school.hei.haapi.model.dto;

import school.hei.haapi.endpoint.rest.model.FeeStatusEnum;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.RetakeExam;

public record RetakeExamParticipantExportDto(
    String ref, String courseCode, Integer totalAmount, FeeStatusEnum status) {
  public static RetakeExamParticipantExportDto from(RetakeExam retakeExam, Fee fee) {
    return new RetakeExamParticipantExportDto(
        retakeExam.getStudent().getRef(),
        retakeExam.getCourse().getCode(),
        fee != null ? fee.getTotalAmount() : null,
        fee != null ? fee.getStatus() : null);
  }
}
