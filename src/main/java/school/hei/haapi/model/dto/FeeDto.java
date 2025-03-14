package school.hei.haapi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import school.hei.haapi.model.Fee;

@Builder
@AllArgsConstructor
@Getter
public class FeeDto {
  private String id;
  private int remainingAmount;
  private int totalAmount;
  private String comment;

  public static FeeDto from(Fee fee) {
    return FeeDto.builder()
        .id(fee.getId())
        .totalAmount(fee.getTotalAmount())
        .comment(fee.getComment())
        .remainingAmount(fee.getRemainingAmount())
        .build();
  }
}
