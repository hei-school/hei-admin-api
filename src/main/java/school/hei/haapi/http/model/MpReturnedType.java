package school.hei.haapi.http.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.Getter;

@Getter
public class MpReturnedType {
  @JsonProperty("transaction_amount")
  Integer transactionAmount;

  @JsonProperty("transactionCreation")
  Instant transactionCreation;

  @JsonProperty("transactionRef")
  String transactionRef;
}
