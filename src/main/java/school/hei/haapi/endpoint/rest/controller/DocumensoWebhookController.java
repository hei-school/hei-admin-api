package school.hei.haapi.endpoint.rest.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.service.DocumensoDocumentService;
import school.hei.haapi.service.documenso.DocumensoWebhookPayload;

@RestController
public class DocumensoWebhookController {
  private final DocumensoDocumentService documensoDocumentService;
  private final String webhookSecret;

  public DocumensoWebhookController(
      DocumensoDocumentService documensoDocumentService,
      @Value("${documenso.webhook.secret}") String webhookSecret) {
    this.documensoDocumentService = documensoDocumentService;
    this.webhookSecret = webhookSecret;
  }

  @PostMapping("/documenso/webhook")
  public ResponseEntity<Void> receiveDocumensoWebhook(
      @RequestHeader(value = "X-Documenso-Secret", required = false) String secret,
      @RequestBody DocumensoWebhookPayload payload) {
    if (!webhookSecret.equals(secret)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    documensoDocumentService.handleWebhook(payload);
    return ResponseEntity.ok().build();
  }
}
