package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static school.hei.haapi.integration.conf.TestUtils.ADMIN1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.MONITOR1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;

import java.io.File;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.rest.api.DocumensoApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.model.CrupdateDocumensoDocument;
import school.hei.haapi.endpoint.rest.model.DocumensoDocumentStatus;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.file.bucket.BucketComponent;
import school.hei.haapi.file.hash.FileHash;
import school.hei.haapi.file.hash.FileHashAlgorithm;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.CycleLevel;
import school.hei.haapi.model.DocumensoDocument;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.DocumensoDocumentRecipientRepository;
import school.hei.haapi.repository.DocumensoDocumentRepository;
import school.hei.haapi.repository.PromotionRepository;
import school.hei.haapi.repository.TemplateDocumensoRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.documenso.DocumensoClient;
import school.hei.haapi.service.documenso.gen.model.TemplateCreateDocumentFromTemplate200Response;
import school.hei.haapi.service.documenso.gen.model.TemplateCreateDocumentFromTemplate200ResponseRecipientsInner;
import school.hei.haapi.service.documenso.gen.model.TemplateFindTemplates200Response;
import school.hei.haapi.service.documenso.gen.model.TemplateFindTemplates200ResponseDataInner;
import school.hei.haapi.service.documenso.gen.model.TemplateGetTemplateById200Response;
import school.hei.haapi.service.documenso.gen.model.TemplateGetTemplateById200ResponseRecipientsInner;

class DocumensoIT extends FacadeITMockedThirdParties {
  @Autowired private UserRepository userRepository;
  @Autowired private PromotionRepository promotionRepository;
  @Autowired private TemplateDocumensoRepository templateDocumensoRepository;
  @Autowired private DocumensoDocumentRepository documensoDocumentRepository;
  @Autowired private DocumensoDocumentRecipientRepository documensoDocumentRecipientRepository;
  @MockBean private DocumensoClient documensoClientMock;
  @MockBean private BucketComponent bucketComponentMock;

  private User admin;
  private User monitor;
  private Promotion promotion;
  private school.hei.haapi.model.TemplateDocumenso template;
  private long templateExternalId;

  @BeforeEach
  void setUp() {
    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
    setUpCognito(cognitoComponentMock);

    // admin1_id / test+admin@hei.school and monitor1_id / test+monitor@hei.school are seeded by
    // src/test/resources/db/testdata (V99_2, V99_29) and shared across the whole test run, so we
    // fetch them rather than inserting new rows with the same email (unique constraint).
    admin = userRepository.findById("admin1_id").orElseThrow();
    admin.setDocumensoUserId(111L);
    admin = userRepository.save(admin);

    monitor = userRepository.findById("monitor1_id").orElseThrow();

    promotion =
        promotionRepository.save(
            Promotion.builder()
                .name("Promo Test")
                .ref("PROMO_" + UUID.randomUUID())
                .startDatetime(Instant.parse("2023-11-01T00:00:00Z"))
                .cycleLevel(CycleLevel.BACHELOR)
                .build());

    templateExternalId = ThreadLocalRandom.current().nextLong(1_000, 1_000_000_000);
    template =
        templateDocumensoRepository.save(
            school.hei.haapi.model.TemplateDocumenso.builder()
                .documensoTemplateId(templateExternalId)
                .title("Attestation")
                .type("PRIVATE")
                .build());
  }

  private DocumensoApi anApi(String token) {
    return new DocumensoApi(anApiClient(token));
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @Test
  void admin_sync_templates_resolves_admin_by_documenso_user_id() throws Exception {
    when(documensoClientMock.findTemplates(isNull(), eq(1), eq(100)))
        .thenReturn(
            new TemplateFindTemplates200Response()
                .addDataItem(
                    new TemplateFindTemplates200ResponseDataInner()
                        .id(BigDecimal.valueOf(777))
                        .title("Certificat")
                        .type(TemplateFindTemplates200ResponseDataInner.TypeEnum.PRIVATE)
                        .userId(BigDecimal.valueOf(111))));

    var result = anApi(ADMIN1_TOKEN).syncDocumensoTemplates();

    assertEquals(1, result.size());
    var synced = result.get(0);
    assertEquals(777L, synced.getDocumensoTemplateId());
    assertEquals("Certificat", synced.getTitle());
    assertEquals(admin.getId(), synced.getAdminId());

    var saved = templateDocumensoRepository.findByDocumensoTemplateId(777L);
    assertTrue(saved.isPresent());
    assertEquals(admin.getId(), saved.get().getAdmin().getId());
  }

  @Test
  void student_sync_templates_ko() {
    assertThrowsForbiddenException(() -> anApi(STUDENT1_TOKEN).syncDocumensoTemplates());
  }

  @Test
  void admin_generate_document_persists_pending_document_with_recipient_tokens() throws Exception {
    when(documensoClientMock.getTemplate(templateExternalId))
        .thenReturn(
            new TemplateGetTemplateById200Response()
                .id(BigDecimal.valueOf(555))
                .title("Attestation")
                .addRecipientsItem(
                    new TemplateGetTemplateById200ResponseRecipientsInner()
                        .id(BigDecimal.valueOf(1))
                        .role(TemplateGetTemplateById200ResponseRecipientsInner.RoleEnum.SIGNER))
                .addRecipientsItem(
                    new TemplateGetTemplateById200ResponseRecipientsInner()
                        .id(BigDecimal.valueOf(2))
                        .role(TemplateGetTemplateById200ResponseRecipientsInner.RoleEnum.SIGNER)));
    when(documensoClientMock.useTemplate(any()))
        .thenReturn(
            new TemplateCreateDocumentFromTemplate200Response()
                .id(BigDecimal.valueOf(999))
                .status(TemplateCreateDocumentFromTemplate200Response.StatusEnum.PENDING)
                .title("Attestation")
                .addRecipientsItem(
                    new TemplateCreateDocumentFromTemplate200ResponseRecipientsInner()
                        .id(BigDecimal.valueOf(1))
                        .email(admin.getEmail())
                        .name("Admin")
                        .token("admin-token"))
                .addRecipientsItem(
                    new TemplateCreateDocumentFromTemplate200ResponseRecipientsInner()
                        .id(BigDecimal.valueOf(2))
                        .email(monitor.getEmail())
                        .name("Monitor")
                        .token("monitor-token")));

    var toCreate =
        new CrupdateDocumensoDocument()
            .promotionId(promotion.getId())
            .level(StudentLevel.L1)
            .documensoTemplateId(templateExternalId)
            .adminId(admin.getId())
            .monitorId(monitor.getId());

    var created = anApi(ADMIN1_TOKEN).generateDocumensoDocument(toCreate);

    assertEquals(DocumensoDocumentStatus.PENDING, created.getStatus());
    assertEquals(999L, created.getDocumensoDocumentId());
    assertEquals(promotion.getId(), created.getPromotionId());

    var adminToken = anApi(ADMIN1_TOKEN).getDocumensoDocumentSigningToken(created.getId());
    assertEquals("admin-token", adminToken.getToken());

    var monitorToken = anApi(MONITOR1_TOKEN).getDocumensoDocumentSigningToken(created.getId());
    assertEquals("monitor-token", monitorToken.getToken());
  }

  @Test
  void student_generate_document_ko() {
    var toCreate =
        new CrupdateDocumensoDocument()
            .promotionId(promotion.getId())
            .level(StudentLevel.L1)
            .documensoTemplateId(templateExternalId)
            .adminId(admin.getId())
            .monitorId(monitor.getId());

    assertThrowsForbiddenException(
        () -> anApi(STUDENT1_TOKEN).generateDocumensoDocument(toCreate));
  }

  @Test
  void webhook_completes_document_and_uploads_signed_pdf_to_s3() throws Exception {
    var pendingDocument =
        documensoDocumentRepository.save(
            DocumensoDocument.builder()
                .documensoDocumentId(4242L)
                .template(template)
                .promotion(promotion)
                .level(StudentLevel.L1)
                .status(DocumensoDocument.Status.PENDING)
                .build());

    var signedFile = File.createTempFile("signed", ".pdf");
    when(documensoClientMock.downloadSignedDocument(4242L)).thenReturn(signedFile);
    when(bucketComponentMock.upload(any(), any()))
        .thenReturn(new FileHash(FileHashAlgorithm.NONE, "dummy"));

    var response =
        sendWebhook("{\"event\":\"DOCUMENT_COMPLETED\",\"payload\":{\"id\":4242}}", "dummy-secret");

    assertEquals(200, response.statusCode());
    var updated = documensoDocumentRepository.findById(pendingDocument.getId()).orElseThrow();
    assertEquals(DocumensoDocument.Status.COMPLETED, updated.getStatus());
    assertNotNull(updated.getFileInfo());
    verify(bucketComponentMock).upload(eq(signedFile), any());
  }

  @Test
  void webhook_with_wrong_secret_is_rejected() throws Exception {
    var response =
        sendWebhook("{\"event\":\"DOCUMENT_COMPLETED\",\"payload\":{\"id\":1}}", "wrong-secret");

    assertEquals(401, response.statusCode());
  }

  private HttpResponse<String> sendWebhook(String jsonBody, String secret) throws Exception {
    var request =
        HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + localPort + "/documenso/webhook"))
            .header("Content-Type", "application/json")
            .header("X-Documenso-Secret", secret)
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();
    return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
  }
}
