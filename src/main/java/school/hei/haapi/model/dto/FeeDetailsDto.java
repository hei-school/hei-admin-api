package school.hei.haapi.model.dto;

import java.time.Instant;
import school.hei.haapi.endpoint.rest.model.FeeCategory;
import school.hei.haapi.endpoint.rest.model.FeeFrequency;
import school.hei.haapi.endpoint.rest.model.FeeStatusEnum;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.mpbs.Mpbs;

public record FeeDetailsDto(
    String ref,
    String firstName,
    String lastName,
    String email,
    int totalAmount,
    int remainingAmount,
    FeeStatusEnum status,
    FeeCategory category,
    FeeFrequency frequency,
    String comment,
    Instant creationDatetime,
    Instant dueDatetime,
    Instant addRefDate,
    Instant successfullyVerifiedAt) {
  public static FeeDetailsDto from(Fee fee, Mpbs mpbs) {
    return new FeeDetailsDto(
        fee.getStudent().getRef(),
        fee.getStudent().getFirstName(),
        fee.getStudent().getLastName(),
        fee.getStudent().getEmail(),
        fee.getTotalAmount(),
        fee.getRemainingAmount(),
        fee.getStatus(),
        fee.getCategory(),
        fee.getFrequency(),
        fee.getComment(),
        fee.getCreationDatetime(),
        fee.getDueDatetime(),
        mpbs != null ? mpbs.getCreationDatetime() : null,
        mpbs != null ? mpbs.getSuccessfullyVerifiedOn() : null);
  }
}
;
