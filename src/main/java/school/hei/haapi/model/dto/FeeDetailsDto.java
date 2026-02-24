package school.hei.haapi.model.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import school.hei.haapi.endpoint.rest.model.FeeCategory;
import school.hei.haapi.endpoint.rest.model.FeeFrequency;
import school.hei.haapi.endpoint.rest.model.FeeStatusEnum;

@Builder
@AllArgsConstructor
@Getter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class FeeDetailsDto {
  private String ref;
  private String firstName;
  private String lastName;
  private String email;
  private int totalAmount;
  private int remainingAmount;
  private FeeStatusEnum status;
  private FeeCategory category;
  private FeeFrequency frequency;
  private String comment;
  private Instant creationDatetime;
  private Instant dueDatetime;
  private Instant addRefDate;
  private Instant successfullyVerificationDate;
}
