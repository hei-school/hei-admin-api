package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static school.hei.haapi.integration.conf.FakeDataProvider.someUser;
import static school.hei.haapi.integration.conf.TestUtils.ADMIN1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.MONITOR1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.model.User.Role.STUDENT;

import java.io.File;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
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
import school.hei.haapi.model.DocumensoDocument;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.DocumensoDocumentRecipientRepository;
import school.hei.haapi.repository.DocumensoDocumentRepository;
import school.hei.haapi.repository.MonitoringStudentRepository;
import school.hei.haapi.repository.TemplateDocumensoRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.documenso.DocumensoClient;
import school.hei.haapi.service.documenso.gen.model.TemplateCreateDocumentFromTemplate200Response;
import school.hei.haapi.service.documenso.gen.model.TemplateCreateDocumentFromTemplate200ResponseRecipientsInner;
import school.hei.haapi.service.documenso.gen.model.TemplateFindTemplates200Response;
import school.hei.haapi.service.documenso.gen.model.TemplateFindTemplates200ResponseDataInner;
import school.hei.haapi.service.documenso.gen.model.TemplateGetTemplateById200Response;
import school.hei.haapi.service.documenso.gen.model.TemplateGetTemplateById200ResponseFieldsInner;
import school.hei.haapi.service.documenso.gen.model.TemplateGetTemplateById200ResponseRecipientsInner;

class DocumensoIT extends FacadeITMockedThirdParties {
  @Autowired private UserRepository userRepository;
  @Autowired private TemplateDocumensoRepository templateDocumensoRepository;
  @Autowired private DocumensoDocumentRepository documensoDocumentRepository;
  @Autowired private DocumensoDocumentRecipientRepository documensoDocumentRecipientRepository;
  @Autowired private MonitoringStudentRepository monitoringStudentRepository;
  @MockBean private DocumensoClient documensoClientMock;
  @MockBean private BucketComponent bucketComponentMock;

  private User admin;
  private User monitor;
  private User student;
  private school.hei.haapi.model.TemplateDocumenso template;
  private long templateExternalId;
  private String templateTitle;

  @BeforeEach
  void setUp() {
    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
    setUpCognito(cognitoComponentMock);
    admin = userRepository.findById("admin1_id").orElseThrow();
    admin.setDocumensoUserId(111L);
    admin = userRepository.save(admin);
    monitor = userRepository.findById("monitor1_id").orElseThrow();
    student =
        userRepository.save(
            someUser("Fanja", STUDENT).toBuilder()
                .nic("101234567890")
                .address("Lot II A 12 Antananarivo")
                .phone("0341234567")
                .build());
    monitoringStudentRepository.saveMonitorFollowingStudents(
        monitor.getId(), List.of(student.getId()), "LINKED");

    templateExternalId = ThreadLocalRandom.current().nextLong(1_000, 1_000_000_000);
    templateTitle = "Fiche d'engagement L1 " + UUID.randomUUID();
    template =
        templateDocumensoRepository.save(
            school.hei.haapi.model.TemplateDocumenso.builder()
                .documensoTemplateId(templateExternalId)
                .title(templateTitle)
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
  void admin_generate_document_prefills_student_data_and_sends_to_monitor() throws Exception {
    when(documensoClientMock.getTemplate(templateExternalId))
        .thenReturn(
            new TemplateGetTemplateById200Response()
                .id(BigDecimal.valueOf(templateExternalId))
                .title(templateTitle)
                .addRecipientsItem(
                    new TemplateGetTemplateById200ResponseRecipientsInner()
                        .id(BigDecimal.valueOf(1))
                        .role(TemplateGetTemplateById200ResponseRecipientsInner.RoleEnum.SIGNER))
                .addFieldsItem(
                    new TemplateGetTemplateById200ResponseFieldsInner()
                        .id(BigDecimal.valueOf(10))
                        .type("TEXT")
                        .label("Nom et prénoms"))
                .addFieldsItem(
                    new TemplateGetTemplateById200ResponseFieldsInner()
                        .id(BigDecimal.valueOf(11))
                        .type("TEXT")
                        .label("Adresse personnelle")));
    when(documensoClientMock.useTemplate(any()))
        .thenReturn(
            new TemplateCreateDocumentFromTemplate200Response()
                .id(BigDecimal.valueOf(999))
                .status(TemplateCreateDocumentFromTemplate200Response.StatusEnum.PENDING)
                .title(templateTitle)
                .addRecipientsItem(
                    new TemplateCreateDocumentFromTemplate200ResponseRecipientsInner()
                        .id(BigDecimal.valueOf(1))
                        .email(monitor.getEmail())
                        .name("Monitor")
                        .token("monitor-token")));

    var toCreate =
        new CrupdateDocumensoDocument().studentId(student.getId()).templateName(templateTitle);

    var created = anApi(ADMIN1_TOKEN).generateDocumensoDocument(toCreate);

    assertEquals(DocumensoDocumentStatus.PENDING, created.getStatus());
    assertEquals(999L, created.getDocumensoDocumentId());
    assertEquals(student.getId(), created.getStudentId());

    var useTemplateCaptor =
        org.mockito.ArgumentCaptor.forClass(
            school.hei.haapi.service.documenso.gen.model.TemplateCreateDocumentFromTemplateRequest
                .class);
    verify(documensoClientMock).useTemplate(useTemplateCaptor.capture());
    var sentRequest = useTemplateCaptor.getValue();
    assertEquals(1, sentRequest.getRecipients().size());
    assertEquals(monitor.getEmail(), sentRequest.getRecipients().get(0).getEmail());
    var prefillByFieldId =
        sentRequest.getPrefillFields().stream()
            .collect(
                java.util.stream.Collectors.toMap(f -> f.getId().longValue(), f -> f.getValue()));
    assertEquals(student.getFirstName() + " " + student.getLastName(), prefillByFieldId.get(10L));
    assertEquals(student.getAddress(), prefillByFieldId.get(11L));

    var monitorToken = anApi(MONITOR1_TOKEN).getDocumensoDocumentSigningToken(created.getId());
    assertEquals("monitor-token", monitorToken.getToken());
  }

  @Test
  void admin_generate_document_fills_topmost_guardian_block_with_monitor_data() throws Exception {
    when(documensoClientMock.getTemplate(templateExternalId))
        .thenReturn(
            new TemplateGetTemplateById200Response()
                .id(BigDecimal.valueOf(templateExternalId))
                .title(templateTitle)
                .addRecipientsItem(
                    new TemplateGetTemplateById200ResponseRecipientsInner()
                        .id(BigDecimal.valueOf(1))
                        .role(TemplateGetTemplateById200ResponseRecipientsInner.RoleEnum.SIGNER))
                .addFieldsItem(
                    new TemplateGetTemplateById200ResponseFieldsInner()
                        .id(BigDecimal.valueOf(20))
                        .type("TEXT")
                        .label("PERE/ MERE/ TUTEUR")
                        .page(BigDecimal.ONE)
                        .positionY(BigDecimal.valueOf(100)))
                .addFieldsItem(
                    new TemplateGetTemplateById200ResponseFieldsInner()
                        .id(BigDecimal.valueOf(21))
                        .type("TEXT")
                        .label("Adresse personnelle")
                        .page(BigDecimal.ONE)
                        .positionY(BigDecimal.valueOf(110)))
                .addFieldsItem(
                    new TemplateGetTemplateById200ResponseFieldsInner()
                        .id(BigDecimal.valueOf(22))
                        .type("TEXT")
                        .label("Téléphones")
                        .page(BigDecimal.ONE)
                        .positionY(BigDecimal.valueOf(120)))
                .addFieldsItem(
                    new TemplateGetTemplateById200ResponseFieldsInner()
                        .id(BigDecimal.valueOf(30))
                        .type("TEXT")
                        .label("Nom et prénoms")
                        .page(BigDecimal.ONE)
                        .positionY(BigDecimal.valueOf(400)))
                .addFieldsItem(
                    new TemplateGetTemplateById200ResponseFieldsInner()
                        .id(BigDecimal.valueOf(31))
                        .type("TEXT")
                        .label("Adresse personnelle")
                        .page(BigDecimal.ONE)
                        .positionY(BigDecimal.valueOf(410)))
                .addFieldsItem(
                    new TemplateGetTemplateById200ResponseFieldsInner()
                        .id(BigDecimal.valueOf(32))
                        .type("TEXT")
                        .label("Téléphones")
                        .page(BigDecimal.ONE)
                        .positionY(BigDecimal.valueOf(420))));
    when(documensoClientMock.useTemplate(any()))
        .thenReturn(
            new TemplateCreateDocumentFromTemplate200Response()
                .id(BigDecimal.valueOf(998))
                .status(TemplateCreateDocumentFromTemplate200Response.StatusEnum.PENDING)
                .title(templateTitle)
                .addRecipientsItem(
                    new TemplateCreateDocumentFromTemplate200ResponseRecipientsInner()
                        .id(BigDecimal.valueOf(1))
                        .email(monitor.getEmail())
                        .name("Monitor")
                        .token("monitor-token")));

    var toCreate =
        new CrupdateDocumensoDocument().studentId(student.getId()).templateName(templateTitle);

    anApi(ADMIN1_TOKEN).generateDocumensoDocument(toCreate);

    var useTemplateCaptor =
        org.mockito.ArgumentCaptor.forClass(
            school.hei.haapi.service.documenso.gen.model.TemplateCreateDocumentFromTemplateRequest
                .class);
    verify(documensoClientMock).useTemplate(useTemplateCaptor.capture());
    var prefillByFieldId =
        useTemplateCaptor.getValue().getPrefillFields().stream()
            .collect(
                java.util.stream.Collectors.toMap(f -> f.getId().longValue(), f -> f.getValue()));

    assertEquals(monitor.getFirstName() + " " + monitor.getLastName(), prefillByFieldId.get(20L));
    assertEquals(monitor.getAddress(), prefillByFieldId.get(21L));
    assertEquals(monitor.getPhone(), prefillByFieldId.get(22L));
    assertEquals(student.getFirstName() + " " + student.getLastName(), prefillByFieldId.get(30L));
    assertEquals(student.getAddress(), prefillByFieldId.get(31L));
    assertEquals(student.getPhone(), prefillByFieldId.get(32L));
  }

  @Test
  void student_generate_document_ko() {
    var toCreate =
        new CrupdateDocumensoDocument().studentId(student.getId()).templateName(templateTitle);

    assertThrowsForbiddenException(() -> anApi(STUDENT1_TOKEN).generateDocumensoDocument(toCreate));
  }

  @Test
  void webhook_completes_document_and_uploads_signed_pdf_to_s3() throws Exception {
    var pendingDocument =
        documensoDocumentRepository.save(
            DocumensoDocument.builder()
                .documensoDocumentId(4242L)
                .template(template)
                .student(student)
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
