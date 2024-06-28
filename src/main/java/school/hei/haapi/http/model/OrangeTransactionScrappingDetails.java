package school.hei.haapi.http.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import school.hei.haapi.endpoint.rest.model.MpbsStatus;
import school.hei.haapi.model.exception.ApiException;

import static school.hei.haapi.endpoint.rest.model.MpbsStatus.FAILED;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.SUCCESS;
import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class OrangeTransactionScrappingDetails {
  private int number;
  private String date;
  private String time;
  private String ref;
  private String status;

  @JsonProperty("client_number")
  private String clientNumber;

  private int amount;

  public MpbsStatus getStatusAsMpbsStatus() {
    return switch (this.status) {
      case "Succès" -> SUCCESS;
      case "Echec" -> FAILED;
      default -> throw new ApiException(SERVER_EXCEPTION, "Unexpected value");
    };
  }
}
