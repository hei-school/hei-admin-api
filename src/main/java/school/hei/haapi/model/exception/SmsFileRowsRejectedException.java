package school.hei.haapi.model.exception;

import java.util.List;
import lombok.Getter;
import school.hei.haapi.endpoint.rest.model.SmsFileImportRejectedRow;

@Getter
public class SmsFileRowsRejectedException extends ApiException {
  private final List<SmsFileImportRejectedRow> rejectedRows;

  public SmsFileRowsRejectedException(List<SmsFileImportRejectedRow> rejectedRows) {
    super(
        ExceptionType.CLIENT_EXCEPTION,
        "%d ligne(s) invalide(s) dans le fichier — aucun message n'a été envoyé, corrige le"
                .formatted(rejectedRows.size())
            + " fichier puis réimporte-le.");
    this.rejectedRows = rejectedRows;
  }
}
