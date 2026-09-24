package school.hei.haapi.service;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.client.RestClientException;
import school.hei.haapi.model.CycleLevel;
import school.hei.haapi.model.DocumensoDocument;
import school.hei.haapi.model.DocumensoDocumentRecipient;
import school.hei.haapi.model.DocumensoDocumentStatus;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.model.TemplateDocumenso;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.ApiException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.DocumensoDocumentRecipientRepository;
import school.hei.haapi.repository.DocumensoDocumentRepository;
import school.hei.haapi.repository.MonitoringStudentRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.dao.DocumensoDocumentDao;
import school.hei.haapi.service.aws.FileService;
import school.hei.haapi.service.documenso.DocumensoClient;
import school.hei.haapi.service.documenso.DocumensoTemplateResolver;
import school.hei.haapi.service.documenso.DocumensoWebhookHandler;
import school.hei.haapi.service.documenso.PrefillFieldsFactory;
import school.hei.haapi.service.documenso.gen.model.TemplateCreateDocumentFromTemplateRequest;
import school.hei.haapi.service.documenso.gen.model.TemplateGetTemplateById200Response;
import school.hei.haapi.service.documenso.gen.model.TemplateGetTemplateById200ResponseRecipientsInner;

class DocumensoDocumentServiceTest {
  private final DocumensoClient documensoClient = mock(DocumensoClient.class);
  private final DocumensoDocumentRepository documentRepository =
      mock(DocumensoDocumentRepository.class);
  private final DocumensoDocumentDao documentDao = mock(DocumensoDocumentDao.class);
  private final DocumensoDocumentRecipientRepository recipientRepository =
      mock(DocumensoDocumentRecipientRepository.class);
  private final UserRepository userRepository = mock(UserRepository.class);
  private final MonitoringStudentRepository monitoringStudentRepository =
      mock(MonitoringStudentRepository.class);
  private final DocumensoTemplateResolver templateResolver = mock(DocumensoTemplateResolver.class);
  private final PrefillFieldsFactory prefillFieldsFactory = mock(PrefillFieldsFactory.class);
  private final DocumensoWebhookHandler webhookHandler = mock(DocumensoWebhookHandler.class);
  private final FileService fileService = mock(FileService.class);
  private final DocumensoFolderService folderService = mock(DocumensoFolderService.class);

  private final DocumensoDocumentService subject =
      new DocumensoDocumentService(
          documensoClient,
          documentRepository,
          documentDao,
          recipientRepository,
          userRepository,
          monitoringStudentRepository,
          templateResolver,
          prefillFieldsFactory,
          webhookHandler,
          fileService,
          folderService);

  private static final String ADMIN_ID = randomUUID().toString();

  private static User aStudent() {
    return User.builder().id(randomUUID().toString()).groupFlows(List.of()).build();
  }

  private void givenTheAskingAdmin() {
    when(userRepository.findById(ADMIN_ID))
        .thenReturn(Optional.of(User.builder().id(ADMIN_ID).build()));
  }

  private void givenStudentWithMonitor(User student) {
    givenTheAskingAdmin();
    when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));
    when(monitoringStudentRepository.findAllMonitorsByStudentId(student.getId()))
        .thenReturn(List.of(User.builder().id(randomUUID().toString()).build()));
    when(templateResolver.resolveByName(anyString(), any()))
        .thenReturn(
            TemplateDocumenso.builder()
                .id(randomUUID().toString())
                .documensoTemplateId(1L)
                .title("Fiche")
                .build());
  }

  @Test
  void a_document_already_asked_for_is_returned_as_is() {
    var student = aStudent();
    givenStudentWithMonitor(student);
    var alreadyThere = DocumensoDocument.builder().documensoDocumentId(999L).build();
    when(documentRepository.findFirstBySubject_IdAndTemplate_IdAndStatusIn(any(), any(), any()))
        .thenReturn(Optional.of(alreadyThere));

    assertEquals(alreadyThere, subject.generateDocument(student.getId(), "Fiche", ADMIN_ID));
    verify(documensoClient, never()).getTemplate(anyLong());
    verify(documensoClient, never()).useTemplate(any());
    verify(documentRepository, never()).save(any());
  }

  @Test
  void only_pending_and_completed_documents_block_a_new_request() {
    var student = aStudent();
    givenStudentWithMonitor(student);
    when(documensoClient.getTemplate(anyLong())).thenThrow(new RestClientException("stop here"));

    assertThrows(
        ApiException.class, () -> subject.generateDocument(student.getId(), "Fiche", ADMIN_ID));

    var statuses = ArgumentCaptor.forClass(Collection.class);
    verify(documentRepository)
        .findFirstBySubject_IdAndTemplate_IdAndStatusIn(any(), any(), statuses.capture());
    assertEquals(
        List.of(DocumensoDocumentStatus.PENDING, DocumensoDocumentStatus.COMPLETED),
        List.copyOf(statuses.getValue()),
        "a rejected document must stay regenerable");
  }

  @Test
  void a_document_never_asked_for_reaches_documenso() {
    var student = aStudent();
    givenStudentWithMonitor(student);
    when(documentRepository.findFirstBySubject_IdAndTemplate_IdAndStatusIn(any(), any(), any()))
        .thenReturn(Optional.empty());
    when(documensoClient.getTemplate(anyLong())).thenThrow(new RestClientException("stop here"));

    assertThrows(
        ApiException.class, () -> subject.generateDocument(student.getId(), "Fiche", ADMIN_ID));
    verify(documensoClient).getTemplate(anyLong());
  }

  @Test
  void an_unknown_asking_admin_is_not_found() {
    var student = aStudent();
    when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));
    when(userRepository.findById("ghost")).thenReturn(Optional.empty());

    var thrown =
        assertThrows(
            NotFoundException.class,
            () -> subject.generateDocument(student.getId(), "Fiche", "ghost"));
    assertTrue(thrown.getMessage().contains("ghost"));
    verify(documensoClient, never()).useTemplate(any());
  }

  @Test
  void an_unknown_student_is_not_found() {
    when(userRepository.findById("nope")).thenReturn(Optional.empty());

    var thrown =
        assertThrows(
            NotFoundException.class, () -> subject.generateDocument("nope", "Fiche", ADMIN_ID));
    assertTrue(thrown.getMessage().contains("nope"));
    verify(documensoClient, never()).useTemplate(any());
  }

  @Test
  void a_student_without_monitor_is_not_found() {
    var student = aStudent();
    givenTheAskingAdmin();
    when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));
    when(monitoringStudentRepository.findAllMonitorsByStudentId(student.getId()))
        .thenReturn(List.of());

    var thrown =
        assertThrows(
            NotFoundException.class,
            () -> subject.generateDocument(student.getId(), "Fiche", ADMIN_ID));
    assertTrue(thrown.getMessage().contains("No monitor linked"));
    verify(documensoClient, never()).useTemplate(any());
  }

  @Test
  void a_template_without_recipient_placeholder_is_a_server_error() {
    var student = aStudent();
    givenStudentWithMonitor(student);
    when(documensoClient.getTemplate(anyLong()))
        .thenReturn(new TemplateGetTemplateById200Response().id(BigDecimal.ONE));

    assertThrows(
        ApiException.class, () -> subject.generateDocument(student.getId(), "Fiche", ADMIN_ID));
    verify(documensoClient, never()).useTemplate(any());
  }

  @Test
  void an_empty_recipient_list_is_a_server_error() {
    var student = aStudent();
    givenStudentWithMonitor(student);
    when(documensoClient.getTemplate(anyLong()))
        .thenReturn(
            new TemplateGetTemplateById200Response().id(BigDecimal.ONE).recipients(List.of()));

    assertThrows(
        ApiException.class, () -> subject.generateDocument(student.getId(), "Fiche", ADMIN_ID));
    verify(documensoClient, never()).useTemplate(any());
  }

  @Test
  void a_documenso_outage_surfaces_as_a_server_error() {
    var student = aStudent();
    givenStudentWithMonitor(student);
    when(documensoClient.getTemplate(anyLong())).thenThrow(new RestClientException("unreachable"));

    assertThrows(
        ApiException.class, () -> subject.generateDocument(student.getId(), "Fiche", ADMIN_ID));
  }

  @Test
  void a_signing_token_is_read_for_its_own_recipient() {
    when(recipientRepository.findByDocument_IdAndUser_Id("doc", "user"))
        .thenReturn(
            Optional.of(DocumensoDocumentRecipient.builder().signingToken("a-token").build()));

    assertEquals("a-token", subject.getSigningToken("doc", "user"));
  }

  @Test
  void a_signing_token_of_someone_else_is_not_found() {
    when(recipientRepository.findByDocument_IdAndUser_Id("doc", "intruder"))
        .thenReturn(Optional.empty());

    var thrown =
        assertThrows(NotFoundException.class, () -> subject.getSigningToken("doc", "intruder"));
    assertTrue(thrown.getMessage().contains("intruder"));
  }

  private static User aStudentOfABachelorPromotion() {
    var promotion =
        Promotion.builder()
            .cycleLevel(CycleLevel.BACHELOR)
            .startDatetime(Instant.now().minus(400, ChronoUnit.DAYS))
            .build();
    return User.builder()
        .id(randomUUID().toString())
        .groupFlows(
            List.of(
                GroupFlow.builder()
                    .groupFlowType(GroupFlow.GroupFlowType.JOIN)
                    .flowDatetime(Instant.now())
                    .group(Group.builder().promotion(promotion).build())
                    .build()))
        .build();
  }

  private void givenAUsableRemoteTemplate() {
    var recipient = new TemplateGetTemplateById200ResponseRecipientsInner();
    recipient.setId(BigDecimal.ONE);
    var remote = new TemplateGetTemplateById200Response();
    remote.setId(BigDecimal.ONE);
    remote.setRecipients(List.of(recipient));
    when(documensoClient.getTemplate(anyLong())).thenReturn(remote);
    when(documentRepository.findFirstBySubject_IdAndTemplate_IdAndStatusIn(any(), any(), any()))
        .thenReturn(Optional.empty());
    /* stops the run right after the request is built, which is all these tests look at */
    when(documensoClient.useTemplate(any())).thenThrow(new RestClientException("stop here"));
  }

  @Test
  void a_fiche_is_filed_under_its_year_and_its_level() {
    var student = aStudentOfABachelorPromotion();
    givenStudentWithMonitor(student);
    givenAUsableRemoteTemplate();
    when(folderService.resolveFolderId(any())).thenReturn("the-folder");

    assertThrows(
        ApiException.class, () -> subject.generateDocument(student.getId(), "Fiche", ADMIN_ID));

    var segments = ArgumentCaptor.forClass(List.class);
    verify(folderService).resolveFolderId(segments.capture());
    var asked = segments.getValue();
    assertEquals(3, asked.size(), "root, academic year, level");
    assertEquals(DocumensoFolderService.ROOT_FOLDER_NAME, asked.get(0));
    assertTrue(asked.get(1).toString().contains(" - "), "an academic year: " + asked.get(1));

    var sent = ArgumentCaptor.forClass(TemplateCreateDocumentFromTemplateRequest.class);
    verify(documensoClient).useTemplate(sent.capture());
    assertEquals("the-folder", sent.getValue().getFolderId());
  }

  @Test
  void a_fiche_whose_folder_cannot_be_resolved_is_still_asked_for() {
    var student = aStudentOfABachelorPromotion();
    givenStudentWithMonitor(student);
    givenAUsableRemoteTemplate();
    when(folderService.resolveFolderId(any())).thenThrow(new RestClientException("documenso down"));

    assertThrows(
        ApiException.class, () -> subject.generateDocument(student.getId(), "Fiche", ADMIN_ID));

    var sent = ArgumentCaptor.forClass(TemplateCreateDocumentFromTemplateRequest.class);
    verify(documensoClient).useTemplate(sent.capture());
    assertNull(
        sent.getValue().getFolderId(),
        "a fiche that cannot be filed is still a fiche that must exist");
  }

  @Test
  void a_student_without_a_promotion_keeps_its_fiche_at_the_root() {
    var student = aStudent();
    givenStudentWithMonitor(student);
    givenAUsableRemoteTemplate();

    assertThrows(
        ApiException.class, () -> subject.generateDocument(student.getId(), "Fiche", ADMIN_ID));

    verifyNoInteractions(folderService);
    var sent = ArgumentCaptor.forClass(TemplateCreateDocumentFromTemplateRequest.class);
    verify(documensoClient).useTemplate(sent.capture());
    assertNull(sent.getValue().getFolderId());
  }
}
