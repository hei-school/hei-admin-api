package school.hei.haapi.endpoint.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import school.hei.haapi.endpoint.rest.model.SmsFileImportRejectedRow;
import school.hei.haapi.model.exception.SmsFileRowsRejectedException;
import school.hei.haapi.model.exception.SmsInsufficientBalanceException;

class InternalToRestExceptionHandlerTest {
  private final InternalToRestExceptionHandler subject = new InternalToRestExceptionHandler();

  @Test
  void maps_insufficient_balance_to_a_400_with_the_alert_body() {
    var exception =
        new SmsInsufficientBalanceException(
            "Solde insuffisant : 0 SMS disponibles pour 3 destinataires demandés.", 0, 3, 0);

    var response = subject.handleSmsInsufficientBalance(exception);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    var body = response.getBody();
    assertEquals("BadRequestException", body.getType());
    assertEquals(exception.getMessage(), body.getMessage());
    assertEquals(0, body.getAvailableBalance());
    assertEquals(3, body.getRecipientCount());
    assertEquals(0, body.getMaxSendableRecipients());
  }

  @Test
  void maps_file_rows_rejected_to_a_400_with_the_rejected_rows() {
    var rejectedRows =
        List.of(
            new SmsFileImportRejectedRow()
                .row(4)
                .value("abc")
                .reason("Ligne 4 ignorée : format de numéro invalide"));
    var exception = new SmsFileRowsRejectedException(rejectedRows);

    var response = subject.handleSmsFileRowsRejected(exception);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    var body = response.getBody();
    assertEquals("BadRequestException", body.getType());
    assertEquals(exception.getMessage(), body.getMessage());
    assertEquals(rejectedRows, body.getRejectedRows());
  }
}
