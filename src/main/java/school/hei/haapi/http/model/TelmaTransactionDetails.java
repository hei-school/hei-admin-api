package school.hei.haapi.http.model;

import java.util.List;
import lombok.Getter;

@Getter
public class TelmaTransactionDetails {
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
