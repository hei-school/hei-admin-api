package school.hei.haapi.http.model;

import java.time.Instant;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import school.hei.haapi.endpoint.rest.model.MpbsStatus;

@Getter
@Builder
@EqualsAndHashCode
@ToString
public class TransactionDetails {
  private Integer pspTransactionAmount;
  private Instant pspDatetimeTransactionCreation;
  private String pspTransactionRef;
  private MpbsStatus status;
}
