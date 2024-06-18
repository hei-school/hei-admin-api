package school.hei.haapi.http.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrangeTransactionScrappingDetails {
  private int number;
  private String date;
  private String time;
  private String ref;
  private String status;

  @JsonProperty("client_number")
  private String clientNumber;

  private int amount;
}
