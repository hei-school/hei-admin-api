package school.hei.haapi.http.model;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TransactionDetails {
  private Integer pspTransactionAmount;
  private Instant pspDatetimeTransactionCreation;
  private String pspTransactionRef;
}
