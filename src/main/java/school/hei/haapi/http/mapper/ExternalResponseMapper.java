package school.hei.haapi.http.mapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.http.model.OrangeTransactionDetails;
import school.hei.haapi.http.model.OrangeTransactionScrappingDetails;
import school.hei.haapi.http.model.TransactionDetails;
import school.hei.haapi.model.MobileTransactionDetails;

@Component
@AllArgsConstructor
public class ExternalResponseMapper {

  public TransactionDetails from(OrangeTransactionDetails orangeRest) {
    return TransactionDetails.builder()
        .pspDatetimeTransactionCreation(orangeRest.getTransactionCreation())
        .pspTransactionRef(orangeRest.getTransactionRef())
        .pspTransactionAmount(orangeRest.getTransactionAmount())
        .build();
  }

  public TransactionDetails from(OrangeTransactionScrappingDetails orangeScrappingRest) {
    return TransactionDetails.builder()
        .pspDatetimeTransactionCreation(
            formatAndGetDateOfTransaction(orangeScrappingRest.getDate()))
        .pspTransactionAmount(orangeScrappingRest.getAmount())
        .pspTransactionRef(orangeScrappingRest.getRef())
        .status(orangeScrappingRest.getStatusAsMpbsStatus())
        .build();
  }

  public TransactionDetails toExternalTransactionDetails(
      MobileTransactionDetails transactionDetails) {
    if (transactionDetails == null) {
      return null;
    }
    return TransactionDetails.builder()
        .pspDatetimeTransactionCreation(transactionDetails.getPspDatetimeTransactionCreation())
        .pspTransactionRef(transactionDetails.getPspTransactionRef())
        .pspTransactionAmount(transactionDetails.getPspTransactionAmount())
        .status(transactionDetails.getStatus())
        .build();
  }

  public MobileTransactionDetails toDomainMobileTransactionDetails(
      TransactionDetails transactionDetails) {
    return MobileTransactionDetails.builder()
        .pspTransactionRef(transactionDetails.getPspTransactionRef())
        .pspTransactionAmount(transactionDetails.getPspTransactionAmount())
        .pspDatetimeTransactionCreation(transactionDetails.getPspDatetimeTransactionCreation())
        .status(transactionDetails.getStatus())
        .build();
  }

  public TransactionDetails toRestMobileTransactionDetails(
      MobileTransactionDetails mobileTransactionDetails) {
    return TransactionDetails.builder()
        .pspTransactionAmount(mobileTransactionDetails.getPspTransactionAmount())
        .pspTransactionRef(mobileTransactionDetails.getPspTransactionRef())
        .pspDatetimeTransactionCreation(
            mobileTransactionDetails.getPspDatetimeTransactionCreation())
        .status(mobileTransactionDetails.getStatus())
        .build();
  }

  private Instant formatAndGetDateOfTransaction(String dateString) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    if (!Objects.isNull(dateString)) {
      LocalDate localDate = LocalDate.parse(dateString, formatter);
      return localDate.atStartOfDay(ZoneId.of("UTC")).toInstant();
    }
    return null;
  }
}
