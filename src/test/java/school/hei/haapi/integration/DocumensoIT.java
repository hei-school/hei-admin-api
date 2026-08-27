package school.hei.haapi.integration;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsApiException;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestAuth.tokenFor;
import static school.hei.haapi.integration.testData.GroupTestData.createGroupFlow;
import static school.hei.haapi.integration.testData.GroupTestData.g1;
import static school.hei.haapi.integration.testData.MonitorTestData.monitorOfAxel;
import static school.hei.haapi.integration.testData.PromotionTestData.aPromotion;
import static school.hei.haapi.integration.testData.StaffTestData.adminMialy;
import static school.hei.haapi.integration.testData.StudentTestData.axel;
import static school.hei.haapi.model.DocumensoDocumentStatus.COMPLETED;
import static school.hei.haapi.model.DocumensoDocumentStatus.PENDING;
import static school.hei.haapi.model.User.Role.STUDENT;
import static school.hei.haapi.model.dto.MonitorStudentLinkDto.Status.LINKED;
import static school.hei.haapi.service.documenso.gen.model.TemplateFindTemplates200ResponseDataInner.TypeEnum.PRIVATE;
import static school.hei.haapi.service.documenso.gen.model.TemplateGetTemplateById200ResponseRecipientsInner.RoleEnum.SIGNER;

import java.io.File;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.DocumensoDocumentGenerationTriggered;
import school.hei.haapi.endpoint.rest.api.DocumensoApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CrupdateDocumensoDocument;
import school.hei.haapi.endpoint.rest.model.DocumensoDocument;
import school.hei.haapi.endpoint.rest.model.DocumensoDocumentStatus;
import school.hei.haapi.endpoint.rest.model.GenerateDocumensoDocuments;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.file.bucket.BucketComponent;
import school.hei.haapi.file.hash.FileHash;
import school.hei.haapi.file.hash.FileHashAlgorithm;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.model.TemplateDocumenso;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.DocumensoDocumentRecipientRepository;
import school.hei.haapi.repository.DocumensoDocumentRepository;
import school.hei.haapi.repository.GroupFlowRepository;
import school.hei.haapi.repository.GroupRepository;
import school.hei.haapi.repository.MonitoringStudentRepository;
import school.hei.haapi.repository.PromotionRepository;
import school.hei.haapi.repository.TemplateDocumensoRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.documenso.DocumensoClient;
import school.hei.haapi.service.documenso.gen.model.*;

class DocumensoIT extends FacadeITMockedThirdParties {
  @Autowired private UserRepository userRepository;
  @Autowired private TemplateDocumensoRepository templateDocumensoRepository;
  @Autowired private DocumensoDocumentRepository documensoDocumentRepository;
  @Autowired private DocumensoDocumentRecipientRepository documensoDocumentRecipientRepository;
  @Autowired private MonitoringStudentRepository monitoringStudentRepository;
  @Autowired private PromotionRepository promotionRepository;
  @Autowired private GroupRepository groupRepository;
  @Autowired private GroupFlowRepository groupFlowRepository;
  @MockBean private DocumensoClient documensoClientMock;
  @MockBean private BucketComponent bucketComponentMock;
  @MockBean private EventProducer eventProducerMock;

  private User admin;
  private User monitor;
  private User student;
  private TemplateDocumenso template;
  private long templateExternalId;
  private long adminDocumensoUserId;
  private String templateTitle;

  private String adminToken;
  private String monitorToken;
  private String studentToken;

  private final List<User> strangers = new ArrayList<>();

  private void setUpTestData() {
    adminDocumensoUserId = ThreadLocalRandom.current().nextLong(1_000, 1_000_000_000);
    admin =
        userRepository.save(adminMialy().toBuilder().documensoUserId(adminDocumensoUserId).build());
    monitor =
        userRepository.save(
            monitorOfAxel().toBuilder().address("Lot II A 12 Antananarivo").build());
    student = userRepository.save(axel());
    monitoringStudentRepository.saveMonitorFollowingStudents(
        monitor.getId(), List.of(student.getId()), LINKED.toString());

    templateExternalId = ThreadLocalRandom.current().nextLong(1_000, 1_000_000_000);
    templateTitle = "Fiche d'engagement L1 " + UUID.randomUUID();
    template =
        templateDocumensoRepository.save(
            TemplateDocumenso.builder()
                .documensoTemplateId(templateExternalId)
                .title(templateTitle)
                .type("PRIVATE")
                .build());
  }

  @BeforeEach
  void setUp() {
    setUpTestData();
    adminToken = tokenFor(casdoorAuthServiceMock, admin);
    monitorToken = tokenFor(casdoorAuthServiceMock, monitor.getEmail(), STUDENT);
    studentToken = tokenFor(casdoorAuthServiceMock, student);
  }

  @AfterEach
  void tearDown() {
    documensoDocumentRecipientRepository.deleteAll();
    documensoDocumentRepository.deleteAll();
    templateDocumensoRepository.deleteAll();
    clearFollowedStudents(monitor.getId());
    strangers.forEach(stranger -> clearFollowedStudents(stranger.getId()));
    userRepository.deleteAll(strangers);
    strangers.clear();
    userRepository.deleteAll(List.of(admin, monitor, student));
  }

  private void clearFollowedStudents(String monitorId) {
    userRepository
        .findById(monitorId)
        .ifPresent(
            followingMonitor -> {
              followingMonitor.setMonitors(new ArrayList<>());
              userRepository.save(followingMonitor);
            });
  }

  private DocumensoApi anApi(String token) {
    return new DocumensoApi(anApiClient(token));
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @Test
  void a_monitor_cannot_ask_for_a_link_on_someone_else_student() throws Exception {
    var otherMonitor = userRepository.save(monitorOfAxel());
    var otherStudent = userRepository.save(axel());
    monitoringStudentRepository.saveMonitorFollowingStudents(
        otherMonitor.getId(), List.of(otherStudent.getId()), LINKED.toString());
    strangers.addAll(List.of(otherMonitor, otherStudent));
    var notMine = generateOneDocumentFor(otherStudent, 4272L);
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"User %s does not follow the subject of document %s\"}"
            .formatted(monitor.getId(), notMine.getId()),
        () -> anApi(monitorToken).getDocumensoDocumentFileUrl(notMine.getId()));
  }

  @Test
  void a_pending_document_has_no_signed_file_to_open() throws Exception {
    var created = generateOneDocumentFor(student, 4270L);
    assertThrows(
        ApiException.class, () -> anApi(monitorToken).getDocumensoDocumentFileUrl(created.getId()));
  }

  @Test
  void student_cannot_ask_for_a_file_link() throws Exception {
    var created = generateOneDocumentFor(student, 4271L);
    assertThrowsForbiddenException(
        () -> anApi(studentToken).getDocumensoDocumentFileUrl(created.getId()));
  }

  @Test
  void admin_lists_the_synced_templates() throws Exception {
    var listed = anApi(adminToken).getDocumensoTemplates(1, 15);

    assertEquals(1, listed.size());
    assertEquals(template.getId(), listed.getFirst().getId());
    assertEquals(templateTitle, listed.getFirst().getTitle());
    assertEquals(templateExternalId, listed.getFirst().getDocumensoTemplateId());
    verifyNoInteractions(documensoClientMock);
  }

  @Test
  void student_cannot_list_the_templates() {
    assertThrowsForbiddenException(() -> anApi(studentToken).getDocumensoTemplates(1, 15));
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
                        .type(PRIVATE)
                        .userId(BigDecimal.valueOf(adminDocumensoUserId))));

    var result = anApi(adminToken).syncDocumensoTemplates();

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
    assertThrowsForbiddenException(() -> anApi(studentToken).syncDocumensoTemplates());
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
                        .role(SIGNER))
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
        new CrupdateDocumensoDocument().subjectId(student.getId()).templateName(templateTitle);
    var created = anApi(adminToken).generateDocumensoDocument(toCreate);

    assertEquals(DocumensoDocumentStatus.PENDING, created.getStatus());
    assertEquals(999L, created.getDocumensoDocumentId());
    assertEquals(student.getId(), created.getSubjectId());

    var useTemplateCaptor =
        ArgumentCaptor.forClass(TemplateCreateDocumentFromTemplateRequest.class);
    verify(documensoClientMock).useTemplate(useTemplateCaptor.capture());
    var sentRequest = useTemplateCaptor.getValue();
    assertEquals(1, sentRequest.getRecipients().size());
    assertEquals(monitor.getEmail(), sentRequest.getRecipients().get(0).getEmail());
    var prefillByFieldId =
        sentRequest.getPrefillFields().stream()
            .collect(Collectors.toMap(f -> f.getId().longValue(), f -> f.getValue()));
    assertEquals(student.getFirstName() + " " + student.getLastName(), prefillByFieldId.get(10L));
    assertEquals(student.getAddress(), prefillByFieldId.get(11L));

    var signingToken = anApi(monitorToken).getDocumensoDocumentSigningToken(created.getId());
    assertEquals("monitor-token", signingToken.getToken());
  }

  @Test
  void admin_generate_document_leaves_the_guardian_block_to_the_monitor() throws Exception {
    when(documensoClientMock.getTemplate(templateExternalId))
        .thenReturn(
            new TemplateGetTemplateById200Response()
                .id(BigDecimal.valueOf(templateExternalId))
                .title(templateTitle)
                .addRecipientsItem(
                    new TemplateGetTemplateById200ResponseRecipientsInner()
                        .id(BigDecimal.valueOf(1))
                        .role(SIGNER))
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
        new CrupdateDocumensoDocument().subjectId(student.getId()).templateName(templateTitle);
    anApi(adminToken).generateDocumensoDocument(toCreate);
    var useTemplateCaptor =
        ArgumentCaptor.forClass(TemplateCreateDocumentFromTemplateRequest.class);
    verify(documensoClientMock).useTemplate(useTemplateCaptor.capture());
    var prefillByFieldId =
        useTemplateCaptor.getValue().getPrefillFields().stream()
            .collect(
                Collectors.toMap(
                    f -> f.getId().longValue(),
                    TemplateCreateDocumentFromTemplateRequestPrefillFieldsInner::getValue));

    assertFalse(prefillByFieldId.containsKey(20L), "the guardian's name is the monitor's to fill");
    assertFalse(
        prefillByFieldId.containsKey(21L), "the guardian's address is the monitor's to fill");
    assertFalse(prefillByFieldId.containsKey(22L), "the guardian's phone is the monitor's to fill");
    assertEquals(student.getFirstName() + " " + student.getLastName(), prefillByFieldId.get(30L));
    assertEquals(student.getAddress(), prefillByFieldId.get(31L));
    assertEquals(student.getPhone(), prefillByFieldId.get(32L));
  }

  @Test
  void student_generate_document_ko() {
    var toCreate =
        new CrupdateDocumensoDocument().subjectId(student.getId()).templateName(templateTitle);
    assertThrowsForbiddenException(() -> anApi(studentToken).generateDocumensoDocument(toCreate));
  }

  @Test
  void monitor_reads_the_documents_of_the_students_it_follows() throws Exception {
    var generated = generateOneDocumentFor(student, 4243L);
    var mine = anApi(monitorToken).getMonitorDocumensoDocuments(monitor.getId(), 1, 15);

    assertEquals(1, mine.size());
    var read = mine.getFirst();
    assertEquals(generated.getId(), read.getId());
    assertEquals(student.getId(), read.getSubjectId());
    assertEquals(DocumensoDocumentStatus.PENDING, read.getStatus());
    assertEquals(admin.getId(), read.getGeneratedById());
    // the table must render a name without one extra call per row
    assertEquals(student.getId(), read.getSubject().getId());
    assertEquals(student.getFirstName(), read.getSubject().getFirstName());
    assertEquals(student.getLastName(), read.getSubject().getLastName());
    assertEquals(student.getRef(), read.getSubject().getRef());
    assertEquals(templateTitle, read.getTemplateTitle());
    assertNull(read.getCompletedDatetime(), "nothing is signed yet");
  }

  @Test
  void a_monitor_cannot_read_another_monitor_documents() throws Exception {
    generateOneDocumentFor(student, 4244L);
    var otherMonitor = userRepository.save(monitorOfAxel());

    assertThrowsForbiddenException(
        () -> anApi(monitorToken).getMonitorDocumensoDocuments(otherMonitor.getId(), 1, 15));
  }

  @Test
  void a_monitor_only_sees_the_documents_of_its_own_students() throws Exception {
    var mineDocument = generateOneDocumentFor(student, 4245L);
    var otherMonitor = userRepository.save(monitorOfAxel());
    var otherStudent = userRepository.save(axel());
    monitoringStudentRepository.saveMonitorFollowingStudents(
        otherMonitor.getId(), List.of(otherStudent.getId()), LINKED.toString());
    strangers.addAll(List.of(otherMonitor, otherStudent));
    var otherDocument = generateOneDocumentFor(otherStudent, 4246L);

    var mine = anApi(monitorToken).getMonitorDocumensoDocuments(monitor.getId(), 1, 15);

    assertEquals(1, mine.size(), "the other monitor's document must not leak in");
    assertEquals(mineDocument.getId(), mine.getFirst().getId());
    assertNotEquals(otherDocument.getId(), mine.getFirst().getId());
  }

  @Test
  void an_admin_reads_any_monitor_documents() throws Exception {
    var generated = generateOneDocumentFor(student, 4247L);
    var read = anApi(adminToken).getMonitorDocumensoDocuments(monitor.getId(), 1, 15);

    assertEquals(1, read.size());
    assertEquals(generated.getId(), read.getFirst().getId());
  }

  @Test
  void a_monitor_without_any_student_reads_an_empty_list() throws Exception {
    generateOneDocumentFor(student, 4248L);
    var lonelyMonitor = userRepository.save(monitorOfAxel());
    strangers.add(lonelyMonitor);
    var lonelyToken = tokenFor(casdoorAuthServiceMock, lonelyMonitor.getEmail(), User.Role.STUDENT);
    var read = anApi(lonelyToken).getMonitorDocumensoDocuments(lonelyMonitor.getId(), 1, 15);

    assertTrue(read.isEmpty());
  }

  @Test
  void documents_are_paged_and_most_recent_first() throws Exception {
    var older = generateOneDocumentFor(student, 4249L);
    var secondStudent = userRepository.save(axel());
    monitoringStudentRepository.saveMonitorFollowingStudents(
        monitor.getId(), List.of(secondStudent.getId()), LINKED.toString());
    strangers.add(secondStudent);
    var newer = generateOneDocumentFor(secondStudent, 4250L);
    var firstPage = anApi(monitorToken).getMonitorDocumensoDocuments(monitor.getId(), 1, 1);
    var secondPage = anApi(monitorToken).getMonitorDocumensoDocuments(monitor.getId(), 2, 1);

    assertEquals(1, firstPage.size(), "a page size of one must return one document");
    assertEquals(1, secondPage.size());
    assertEquals(newer.getId(), firstPage.getFirst().getId(), "most recent comes first");
    assertEquals(older.getId(), secondPage.getFirst().getId());
  }

  @Test
  void admin_launches_a_bulk_generation_for_a_promotion() throws Exception {
    var promotion = aPromotionOfTwoStudents();
    var launched =
        anApi(adminToken)
            .generateDocumensoDocumentsForPromotion(
                promotion.getId(), new GenerateDocumensoDocuments().templateName(templateTitle));

    assertEquals(promotion.getId(), launched.getPromotionId());
    assertEquals(templateTitle, launched.getTemplateName());
    assertEquals(2, launched.getStudentCount(), "one per student of the promotion");

    var fired = ArgumentCaptor.forClass(Collection.class);
    verify(eventProducerMock, times(2)).accept(fired.capture());
    var events =
        fired.getAllValues().stream()
            .flatMap(collection -> ((Collection<?>) collection).stream())
            .map(DocumensoDocumentGenerationTriggered.class::cast)
            .toList();
    assertTrue(
        events.stream().allMatch(e -> admin.getId().equals(e.getGeneratedById())),
        "the asking admin must ride along every event");
    assertTrue(events.stream().allMatch(e -> templateTitle.equals(e.getTemplateName())));
  }

  @Test
  void bulk_generation_on_an_unknown_promotion_is_not_found() {
    var body = new GenerateDocumensoDocuments().templateName(templateTitle);

    assertThrows(
        ApiException.class,
        () -> anApi(adminToken).generateDocumensoDocumentsForPromotion("not-a-promotion", body));
    verifyNoInteractions(eventProducerMock);
  }

  @Test
  void student_cannot_launch_a_bulk_generation() throws Exception {
    var promotion = aPromotionOfTwoStudents();
    var body = new GenerateDocumensoDocuments().templateName(templateTitle);

    assertThrowsForbiddenException(
        () -> anApi(studentToken).generateDocumensoDocumentsForPromotion(promotion.getId(), body));
    verifyNoInteractions(eventProducerMock);
  }

  @Test
  void admin_lists_only_the_documents_of_that_promotion() throws Exception {
    var promotion = aPromotionOfTwoStudents();
    var enrolled = userRepository.findAllStudentsByPromotionId(promotion.getId());
    var inside = generateOneDocumentFor(enrolled.getFirst(), 4251L);
    var outside = generateOneDocumentFor(student, 4252L);

    var listed =
        anApi(adminToken).getPromotionDocumensoDocuments(promotion.getId(), null, null, 1, 15);

    assertEquals(1, listed.size(), "a document outside the promotion must not leak in");
    assertEquals(inside.getId(), listed.getFirst().getId());
    assertNotEquals(outside.getId(), listed.getFirst().getId());
  }

  @Test
  void promotion_documents_can_be_filtered_by_status() throws Exception {
    var promotion = aPromotionOfTwoStudents();
    var enrolled = userRepository.findAllStudentsByPromotionId(promotion.getId());
    generateOneDocumentFor(enrolled.getFirst(), 4253L);

    var pending =
        anApi(adminToken)
            .getPromotionDocumensoDocuments(
                promotion.getId(), null, DocumensoDocumentStatus.PENDING, 1, 15);
    var completed =
        anApi(adminToken)
            .getPromotionDocumensoDocuments(
                promotion.getId(), null, DocumensoDocumentStatus.COMPLETED, 1, 15);

    assertEquals(1, pending.size());
    assertTrue(completed.isEmpty(), "nothing is signed yet, so the filter must exclude it");
  }

  @Test
  void a_promotion_without_documents_lists_nothing() throws Exception {
    var promotion = aPromotionOfTwoStudents();

    var listed =
        anApi(adminToken).getPromotionDocumensoDocuments(promotion.getId(), null, null, 1, 15);

    assertTrue(listed.isEmpty());
  }

  @Test
  void student_cannot_list_promotion_documents() {
    var promotion = aPromotionOfTwoStudents();

    assertThrowsForbiddenException(
        () ->
            anApi(studentToken)
                .getPromotionDocumensoDocuments(promotion.getId(), null, null, 1, 15));
  }

  private Promotion aPromotionOfTwoStudents() {
    var promotion =
        promotionRepository.save(aPromotion("Promo " + randomUUID(), "P" + randomUUID()));
    var newGroup = g1();
    newGroup.setPromotion(promotion);
    var group = groupRepository.save(newGroup);
    var firstStudent = userRepository.save(axel());
    var secondStudent = userRepository.save(axel());
    var theirMonitor = userRepository.save(monitorOfAxel());
    monitoringStudentRepository.saveMonitorFollowingStudents(
        theirMonitor.getId(),
        List.of(firstStudent.getId(), secondStudent.getId()),
        LINKED.toString());
    strangers.addAll(List.of(firstStudent, secondStudent, theirMonitor));
    groupFlowRepository.saveAll(
        List.of(createGroupFlow(firstStudent, group), createGroupFlow(secondStudent, group)));
    return promotion;
  }

  private DocumensoDocument generateOneDocumentFor(User forStudent, long documensoDocumentId)
      throws Exception {
    when(documensoClientMock.getTemplate(templateExternalId))
        .thenReturn(
            new TemplateGetTemplateById200Response()
                .id(BigDecimal.valueOf(templateExternalId))
                .title(templateTitle)
                .addRecipientsItem(
                    new TemplateGetTemplateById200ResponseRecipientsInner()
                        .id(BigDecimal.valueOf(1))
                        .role(TemplateGetTemplateById200ResponseRecipientsInner.RoleEnum.SIGNER)));
    when(documensoClientMock.useTemplate(any()))
        .thenReturn(
            new TemplateCreateDocumentFromTemplate200Response()
                .id(BigDecimal.valueOf(documensoDocumentId))
                .status(TemplateCreateDocumentFromTemplate200Response.StatusEnum.PENDING)
                .title(templateTitle)
                .addRecipientsItem(
                    new TemplateCreateDocumentFromTemplate200ResponseRecipientsInner()
                        .id(BigDecimal.valueOf(1))
                        .email(monitor.getEmail())
                        .name("Monitor")
                        .token("monitor-token")));

    return anApi(adminToken)
        .generateDocumensoDocument(
            new CrupdateDocumensoDocument()
                .subjectId(forStudent.getId())
                .templateName(templateTitle));
  }

  @Test
  void webhook_completes_document_and_uploads_signed_pdf_to_s3() throws Exception {
    var pendingDocument =
        documensoDocumentRepository.save(
            school.hei.haapi.model.DocumensoDocument.builder()
                .documensoDocumentId(4242L)
                .template(template)
                .subject(student)
                .level(StudentLevel.L1)
                .status(PENDING)
                .build());

    var signedFile = File.createTempFile("signed", ".pdf");
    when(documensoClientMock.downloadSignedDocument(4242L)).thenReturn(signedFile);
    when(bucketComponentMock.upload(any(), any()))
        .thenReturn(new FileHash(FileHashAlgorithm.NONE, "dummy"));
    var response =
        sendWebhook("{\"event\":\"DOCUMENT_COMPLETED\",\"payload\":{\"id\":4242}}", "dummy-secret");

    assertEquals(200, response.statusCode());
    var updated = documensoDocumentRepository.findById(pendingDocument.getId()).orElseThrow();
    assertEquals(COMPLETED, updated.getStatus());
    assertNotNull(updated.getFileInfo());
    verify(bucketComponentMock).upload(eq(signedFile), any());

    // the signature date must reach the front, otherwise no "signed on" column is possible
    var asRead =
        anApi(monitorToken).getMonitorDocumensoDocuments(monitor.getId(), 1, 15).getFirst();
    assertEquals(DocumensoDocumentStatus.COMPLETED, asRead.getStatus());
    assertNotNull(asRead.getCompletedDatetime());
    assertEquals(student.getRef(), asRead.getSubject().getRef());
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
