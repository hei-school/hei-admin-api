package school.hei.haapi.endpoint.rest.http.type;

import java.util.List;
import lombok.Getter;

@Getter
public class TransactionDetails {
  private Integer amount;
  private String currency;
  private String transactionReference;
  private String transactionStatus;
  private String createDate;
  private List<KeyValue> debitParty;
  private List<KeyValue> creditParty;
  private List<KeyValue> metadata;
  private FeeTyped fee;
}
