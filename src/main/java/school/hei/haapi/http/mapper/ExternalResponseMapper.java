package school.hei.haapi.http.mapper;

import java.time.Instant;
import org.springframework.stereotype.Component;
import school.hei.haapi.http.model.OrangeTransactionDetails;
import school.hei.haapi.http.model.TelmaTransactionDetails;
import school.hei.haapi.http.model.TransactionDetails;

@Component
public class ExternalResponseMapper {
  public TransactionDetails from(OrangeTransactionDetails orangeRest) {
    return TransactionDetails.builder()
        .pspDatetimeTransactionCreation(orangeRest.getTransactionCreation())
        .pspTransactionRef(orangeRest.getTransactionRef())
        .pspTransactionAmount(orangeRest.getTransactionAmount())
        .build();
  }

  public TransactionDetails from(TelmaTransactionDetails telmaRest) {
    return TransactionDetails.builder()
        .pspDatetimeTransactionCreation(Instant.parse(telmaRest.getCreateDate()))
        .pspTransactionRef(telmaRest.getTransactionReference())
        .pspTransactionAmount(telmaRest.getAmount())
        .build();
  }
}
