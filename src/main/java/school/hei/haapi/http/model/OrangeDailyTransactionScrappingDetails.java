package school.hei.haapi.http.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrangeDailyTransactionScrappingDetails {
  @JsonProperty("transactionDate")
  private String transactionDate;

  private Instant timestamp;

  private List<OrangeTransactionScrappingDetails> transactions;
}
