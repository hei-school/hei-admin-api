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
import school.hei.haapi.http.model.TelmaTransactionDetails;
import school.hei.haapi.http.model.TransactionDetails;
import school.hei.haapi.model.MobileTransactionDetails;
import school.hei.haapi.repository.MpbsRepository;

@Component
@AllArgsConstructor
public class ExternalResponseMapper {
  private final MpbsRepository mpbsRepository;

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

  public TransactionDetails from(OrangeTransactionScrappingDetails orangeScrappingRest) {
    return TransactionDetails.builder()
        .pspDatetimeTransactionCreation(
            formatAndGetDateOfTransaction(orangeScrappingRest.getDate()))
        .pspTransactionAmount(orangeScrappingRest.getAmount())
        .pspTransactionRef(orangeScrappingRest.getRef())
        .build();
  }

  public TransactionDetails toExternalTransactionDetails(
      MobileTransactionDetails transactionDetails) {
    return TransactionDetails.builder()
        .pspDatetimeTransactionCreation(transactionDetails.getPspDatetimeTransactionCreation())
        .pspTransactionRef(transactionDetails.getPspTransactionRef())
        .pspTransactionAmount(transactionDetails.getPspTransactionAmount())
        .build();
  }

  public MobileTransactionDetails toDomainMobileTransactionDetails(
      TransactionDetails transactionDetails) {
    String studentRef = findStudentRefByTransactionDetails(transactionDetails);
    return MobileTransactionDetails.builder()
        .pspTransactionRef(transactionDetails.getPspTransactionRef())
        .pspTransactionAmount(transactionDetails.getPspTransactionAmount())
        .pspDatetimeTransactionCreation(transactionDetails.getPspDatetimeTransactionCreation())
        .studentRef(studentRef)
        .build();
  }

  public TransactionDetails toRestMobileTransactionDetails(
      MobileTransactionDetails mobileTransactionDetails) {
    return TransactionDetails.builder()
        .pspTransactionAmount(mobileTransactionDetails.getPspTransactionAmount())
        .pspTransactionRef(mobileTransactionDetails.getPspTransactionRef())
        .pspDatetimeTransactionCreation(
            mobileTransactionDetails.getPspDatetimeTransactionCreation())
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

  private String findStudentRefByTransactionDetails(TransactionDetails transactionDetails) {
    return mpbsRepository
        .findByPspId(transactionDetails.getPspTransactionRef())
        .getStudent()
        .getRef();
  }
}
