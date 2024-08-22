package school.hei.haapi.service;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.model.MpbsStatus;
import school.hei.haapi.http.mapper.ExternalResponseMapper;
import school.hei.haapi.http.model.OrangeTransactionScrappingDetails;
import school.hei.haapi.http.model.TransactionDetails;

class ExternalResponseMapperTest {
  ExternalResponseMapper subject;

  @BeforeEach
  void setUp() {
    subject = new ExternalResponseMapper();
  }

  @Test
  void from() {
    var actualTransactionDetails = subject.from(mockedOrangeTransactionScrappingDetails());
    assertEquals(expectedTransactionDetails(), actualTransactionDetails);
  }

  private TransactionDetails expectedTransactionDetails() {
    return TransactionDetails.builder()
        .pspTransactionRef("ref")
        .pspOwnDatetimeVerification(Instant.parse("2024-08-11T15:29:27.00Z"))
        .pspTransactionAmount(3000)
        .pspDatetimeTransactionCreation(Instant.parse("2024-08-11T00:00:00.00Z"))
        .status(MpbsStatus.SUCCESS)
        .build();
  }

  private OrangeTransactionScrappingDetails mockedOrangeTransactionScrappingDetails() {
    return OrangeTransactionScrappingDetails.builder()
        .ref("ref")
        .date("11/08/2024")
        .number(1)
        .amount(3000)
        .clientNumber("0324987567")
        .time("15:29:27")
        .status("Succès")
        .build();
  }
}
