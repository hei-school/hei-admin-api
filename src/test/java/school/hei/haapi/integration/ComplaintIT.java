package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static school.hei.haapi.integration.conf.ApiAssertions.assertBadRequestException;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsApiException;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.FakeDataProvider.someStudent;
import static school.hei.haapi.integration.conf.FakeDataProvider.someUser;
import static school.hei.haapi.integration.conf.TestAuth.tokenFor;
import static school.hei.haapi.integration.conf.TestFiles.uploadComplaintAttachment;
import static school.hei.haapi.integration.conf.TestMocks.setUpS3Service;
import static school.hei.haapi.model.User.Role.ADMIN;
import static school.hei.haapi.model.User.Role.MANAGER;
import static school.hei.haapi.model.User.Role.MONITOR;
import static school.hei.haapi.model.User.Role.TEACHER;
import static school.hei.haapi.model.dto.MonitorStudentLinkDto.Status.LINKED;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.ComplaintEmailNotificationRequested;
import school.hei.haapi.endpoint.event.model.ComplaintSmsNotificationRequested;
import school.hei.haapi.endpoint.rest.api.ComplaintApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.ComplaintAttachment;
import school.hei.haapi.endpoint.rest.model.ComplaintStatus;
import school.hei.haapi.endpoint.rest.model.ComplaintType;
import school.hei.haapi.endpoint.rest.model.CrupdateComplaint;
import school.hei.haapi.endpoint.rest.model.CrupdateComplaintRouting;
import school.hei.haapi.endpoint.rest.model.UpdateComplaintAssignees;
import school.hei.haapi.endpoint.rest.model.UpdateComplaintStatus;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestFiles;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.MonitoringStudentRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.ComplaintRoutingService;

class ComplaintIT extends FacadeITMockedThirdParties {
  @Autowired private UserRepository userRepository;
  @Autowired private MonitoringStudentRepository monitoringStudentRepository;
  @Autowired private ComplaintRoutingService complaintRoutingService;
  @Autowired private ObjectMapper objectMapper;
  @MockBean private EventProducer<ComplaintEmailNotificationRequested> emailEventProducerMock;
  @MockBean private EventProducer<ComplaintSmsNotificationRequested> smsEventProducerMock;

  private User axel;
  private User tolotra;
  private User manager;
  private User admin;
  private User teacher;
  private User monitorOfAxel;
  private User unlinkedMonitor;
  private String axelToken;
  private String tolotraToken;
  private String managerToken;
  private String adminToken;
  private String teacherToken;
  private String monitorOfAxelToken;
  private String unlinkedMonitorToken;

  @BeforeEach
  void setUp() {
    axel = userRepository.save(someStudent("axel"));
    tolotra = userRepository.save(someStudent("tolotra"));
    manager = userRepository.save(someUser("manager", MANAGER));
    admin = userRepository.save(someUser("admin", ADMIN));
    teacher = userRepository.save(someUser("teacher", TEACHER));
    monitorOfAxel = userRepository.save(someUser("monitor-of-axel", MONITOR));
    unlinkedMonitor = userRepository.save(someUser("unlinked-monitor", MONITOR));
    monitoringStudentRepository.saveMonitorFollowingStudents(
        monitorOfAxel.getId(), List.of(axel.getId()), LINKED.toString());

    complaintRoutingService.create(school.hei.haapi.model.ComplaintType.ACADEMIC, manager);

    axelToken = tokenFor(casdoorAuthServiceMock, axel);
    tolotraToken = tokenFor(casdoorAuthServiceMock, tolotra);
    managerToken = tokenFor(casdoorAuthServiceMock, manager);
    adminToken = tokenFor(casdoorAuthServiceMock, admin);
    teacherToken = tokenFor(casdoorAuthServiceMock, teacher);
    monitorOfAxelToken = tokenFor(casdoorAuthServiceMock, monitorOfAxel);
    unlinkedMonitorToken = tokenFor(casdoorAuthServiceMock, unlinkedMonitor);

    setUpS3Service(fileService);
  }

  @Test
  void student_create_complaint_ok() throws ApiException {
    var api = new ComplaintApi(anApiClient(axelToken));
    var dto =
        new CrupdateComplaint()
            .type(ComplaintType.ACADEMIC)
            .title("Note erronée")
            .description("Ma note de mathématiques est incorrecte.");

    var created = api.createComplaint(dto);

    assertNotNull(created.getId());
    assertEquals(ComplaintType.ACADEMIC, created.getType());
    assertEquals(ComplaintStatus.CREATED, created.getStatus());
    assertEquals(axel.getId(), created.getStudent().getId());
    assertEquals(axel.getId(), created.getCreatedBy().getId());
    assertEquals(1, created.getAssignees().size());
    assertEquals(manager.getId(), created.getAssignees().getFirst().getId());

    verify(emailEventProducerMock, times(1)).accept(anyList());
  }

  @Test
  void student_create_complaint_without_routing_ko() {
    var api = new ComplaintApi(anApiClient(axelToken));
    var dto =
        new CrupdateComplaint()
            .type(ComplaintType.FINANCIAL)
            .title("Facture incorrecte")
            .description("Le montant facturé ne correspond pas.");

    assertThrowsApiException(
        "{\"type\":\"404 NOT_FOUND\",\"message\":\"No manager is routed for complaint type"
            + " #FINANCIAL\"}",
        () -> api.createComplaint(dto));
  }

  @Test
  void create_complaint_is_always_for_the_calling_student_ok() throws ApiException {
    var api = new ComplaintApi(anApiClient(tolotraToken));
    var dto =
        new CrupdateComplaint()
            .type(ComplaintType.ACADEMIC)
            .title("Titre")
            .description("Description");

    var created = api.createComplaint(dto);

    assertEquals(tolotra.getId(), created.getStudent().getId());
  }

  @Test
  void manager_list_and_filter_complaints_ok() throws ApiException {
    var studentApi = new ComplaintApi(anApiClient(axelToken));
    var complaint =
        studentApi.createComplaint(
            new CrupdateComplaint()
                .type(ComplaintType.ACADEMIC)
                .title("Titre")
                .description("Description"));

    var managerApi = new ComplaintApi(anApiClient(managerToken));
    var all = managerApi.getComplaints(null, null, null, null, null, null, null, null);
    assertTrue(all.stream().anyMatch(c -> c.getId().equals(complaint.getId())));

    var filteredByManager =
        managerApi.getComplaints(null, null, null, null, null, null, manager.getId(), null);
    assertTrue(filteredByManager.stream().anyMatch(c -> c.getId().equals(complaint.getId())));

    var filteredByStatus =
        managerApi.getComplaints(
            null, null, null, null, null, List.of(ComplaintStatus.REJECTED), null, null);
    assertTrue(filteredByStatus.stream().noneMatch(c -> c.getId().equals(complaint.getId())));
  }

  @Test
  void student_cannot_list_all_complaints_ko() {
    var api = new ComplaintApi(anApiClient(axelToken));
    assertThrowsForbiddenException(
        () -> api.getComplaints(null, null, null, null, null, null, null, null));
  }

  @Test
  void student_get_own_complaint_by_id_ok() throws ApiException {
    var studentApi = new ComplaintApi(anApiClient(axelToken));
    var complaint =
        studentApi.createComplaint(
            new CrupdateComplaint()
                .type(ComplaintType.ACADEMIC)
                .title("Titre")
                .description("Description"));

    var fetched = studentApi.getComplaintById(complaint.getId());

    assertEquals(complaint.getId(), fetched.getId());
  }

  @Test
  void student_get_other_student_complaint_ko() throws ApiException {
    var axelApi = new ComplaintApi(anApiClient(axelToken));
    var complaint =
        axelApi.createComplaint(
            new CrupdateComplaint()
                .type(ComplaintType.ACADEMIC)
                .title("Titre")
                .description("Description"));

    var tolotraApi = new ComplaintApi(anApiClient(tolotraToken));
    assertThrowsForbiddenException(() -> tolotraApi.getComplaintById(complaint.getId()));
  }

  @Test
  void manager_add_assignees_ok() throws ApiException {
    var studentApi = new ComplaintApi(anApiClient(axelToken));
    var complaint =
        studentApi.createComplaint(
            new CrupdateComplaint()
                .type(ComplaintType.ACADEMIC)
                .title("Titre")
                .description("Description"));

    var managerApi = new ComplaintApi(anApiClient(managerToken));
    var updated =
        managerApi.addComplaintAssignees(
            complaint.getId(), new UpdateComplaintAssignees().userIds(List.of(admin.getId())));

    assertEquals(2, updated.getAssignees().size());
    assertTrue(updated.getAssignees().stream().anyMatch(u -> u.getId().equals(admin.getId())));
  }

  @Test
  void manager_add_assignees_moves_created_to_assigned_ok() throws ApiException {
    var studentApi = new ComplaintApi(anApiClient(axelToken));
    var complaint =
        studentApi.createComplaint(
            new CrupdateComplaint()
                .type(ComplaintType.ACADEMIC)
                .title("Titre")
                .description("Description"));

    var managerApi = new ComplaintApi(anApiClient(managerToken));
    var updated =
        managerApi.addComplaintAssignees(
            complaint.getId(), new UpdateComplaintAssignees().userIds(List.of(admin.getId())));

    assertEquals(ComplaintStatus.ASSIGNED, updated.getStatus());

    var history = managerApi.getComplaintStatusHistory(complaint.getId(), null, null, null, null);
    assertEquals(2, history.size());
    assertEquals(ComplaintStatus.ASSIGNED, history.getFirst().getStatus());
  }

  @Test
  void manager_add_assignees_does_not_change_status_once_in_progress_ok() throws ApiException {
    var studentApi = new ComplaintApi(anApiClient(axelToken));
    var complaint =
        studentApi.createComplaint(
            new CrupdateComplaint()
                .type(ComplaintType.ACADEMIC)
                .title("Titre")
                .description("Description"));

    var managerApi = new ComplaintApi(anApiClient(managerToken));
    managerApi.updateComplaintStatus(
        complaint.getId(), new UpdateComplaintStatus().status(ComplaintStatus.IN_PROGRESS));

    var updated =
        managerApi.addComplaintAssignees(
            complaint.getId(), new UpdateComplaintAssignees().userIds(List.of(admin.getId())));

    assertEquals(ComplaintStatus.IN_PROGRESS, updated.getStatus());
  }

  @Test
  void manager_moves_assigned_complaint_to_in_progress_ok() throws ApiException {
    var studentApi = new ComplaintApi(anApiClient(axelToken));
    var complaint =
        studentApi.createComplaint(
            new CrupdateComplaint()
                .type(ComplaintType.ACADEMIC)
                .title("Titre")
                .description("Description"));

    var managerApi = new ComplaintApi(anApiClient(managerToken));
    managerApi.addComplaintAssignees(
        complaint.getId(), new UpdateComplaintAssignees().userIds(List.of(admin.getId())));

    var inProgress =
        managerApi.updateComplaintStatus(
            complaint.getId(), new UpdateComplaintStatus().status(ComplaintStatus.IN_PROGRESS));

    assertEquals(ComplaintStatus.IN_PROGRESS, inProgress.getStatus());
  }

  @Test
  void manager_rejects_assigned_complaint_with_comment_ok() throws ApiException {
    var studentApi = new ComplaintApi(anApiClient(axelToken));
    var complaint =
        studentApi.createComplaint(
            new CrupdateComplaint()
                .type(ComplaintType.ACADEMIC)
                .title("Titre")
                .description("Description"));

    var managerApi = new ComplaintApi(anApiClient(managerToken));
    managerApi.addComplaintAssignees(
        complaint.getId(), new UpdateComplaintAssignees().userIds(List.of(admin.getId())));

    var rejected =
        managerApi.updateComplaintStatus(
            complaint.getId(),
            new UpdateComplaintStatus().status(ComplaintStatus.REJECTED).comment("Non fondée."));

    assertEquals(ComplaintStatus.REJECTED, rejected.getStatus());
  }

  @Test
  void teacher_sees_only_assigned_complaints_in_list_ok() throws ApiException {
    var studentApi = new ComplaintApi(anApiClient(axelToken));
    var assignedToTeacher =
        studentApi.createComplaint(
            new CrupdateComplaint()
                .type(ComplaintType.ACADEMIC)
                .title("Assignée au teacher")
                .description("Description"));
    var notAssignedToTeacher =
        studentApi.createComplaint(
            new CrupdateComplaint()
                .type(ComplaintType.ACADEMIC)
                .title("Pas assignée au teacher")
                .description("Description"));

    var managerApi = new ComplaintApi(anApiClient(managerToken));
    managerApi.addComplaintAssignees(
        assignedToTeacher.getId(),
        new UpdateComplaintAssignees().userIds(List.of(teacher.getId())));

    var teacherApi = new ComplaintApi(anApiClient(teacherToken));
    var teacherComplaints =
        teacherApi.getComplaints(null, null, null, null, null, null, null, null);

    assertTrue(
        teacherComplaints.stream().anyMatch(c -> c.getId().equals(assignedToTeacher.getId())));
    assertTrue(
        teacherComplaints.stream().noneMatch(c -> c.getId().equals(notAssignedToTeacher.getId())));
  }

  @Test
  void teacher_can_get_assigned_complaint_by_id_ok() throws ApiException {
    var studentApi = new ComplaintApi(anApiClient(axelToken));
    var complaint =
        studentApi.createComplaint(
            new CrupdateComplaint()
                .type(ComplaintType.ACADEMIC)
                .title("Titre")
                .description("Description"));

    var managerApi = new ComplaintApi(anApiClient(managerToken));
    managerApi.addComplaintAssignees(
        complaint.getId(), new UpdateComplaintAssignees().userIds(List.of(teacher.getId())));

    var teacherApi = new ComplaintApi(anApiClient(teacherToken));
    var fetched = teacherApi.getComplaintById(complaint.getId());

    assertEquals(complaint.getId(), fetched.getId());
  }

  @Test
  void teacher_cannot_get_unassigned_complaint_by_id_ko() throws ApiException {
    var studentApi = new ComplaintApi(anApiClient(axelToken));
    var complaint =
        studentApi.createComplaint(
            new CrupdateComplaint()
                .type(ComplaintType.ACADEMIC)
                .title("Titre")
                .description("Description"));

    var teacherApi = new ComplaintApi(anApiClient(teacherToken));
    assertThrowsForbiddenException(() -> teacherApi.getComplaintById(complaint.getId()));
  }

  @Test
  void teacher_can_update_status_of_assigned_complaint_ok() throws ApiException {
    var studentApi = new ComplaintApi(anApiClient(axelToken));
    var complaint =
        studentApi.createComplaint(
            new CrupdateComplaint()
                .type(ComplaintType.ACADEMIC)
                .title("Titre")
                .description("Description"));

    var managerApi = new ComplaintApi(anApiClient(managerToken));
    managerApi.addComplaintAssignees(
        complaint.getId(), new UpdateComplaintAssignees().userIds(List.of(teacher.getId())));

    var teacherApi = new ComplaintApi(anApiClient(teacherToken));
    var inProgress =
        teacherApi.updateComplaintStatus(
            complaint.getId(), new UpdateComplaintStatus().status(ComplaintStatus.IN_PROGRESS));

    assertEquals(ComplaintStatus.IN_PROGRESS, inProgress.getStatus());
  }

  @Test
  void teacher_cannot_update_status_of_unassigned_complaint_ko() throws ApiException {
    var studentApi = new ComplaintApi(anApiClient(axelToken));
    var complaint =
        studentApi.createComplaint(
            new CrupdateComplaint()
                .type(ComplaintType.ACADEMIC)
                .title("Titre")
                .description("Description"));

    var teacherApi = new ComplaintApi(anApiClient(teacherToken));
    assertThrowsForbiddenException(
        () ->
            teacherApi.updateComplaintStatus(
                complaint.getId(),
                new UpdateComplaintStatus().status(ComplaintStatus.IN_PROGRESS)));
  }

  @Test
  void manager_update_status_in_progress_then_resolved_ok() throws ApiException {
    var studentApi = new ComplaintApi(anApiClient(axelToken));
    var complaint =
        studentApi.createComplaint(
            new CrupdateComplaint()
                .type(ComplaintType.ACADEMIC)
                .title("Titre")
                .description("Description"));

    var managerApi = new ComplaintApi(anApiClient(managerToken));
    var inProgress =
        managerApi.updateComplaintStatus(
            complaint.getId(), new UpdateComplaintStatus().status(ComplaintStatus.IN_PROGRESS));
    assertEquals(ComplaintStatus.IN_PROGRESS, inProgress.getStatus());

    var resolved =
        managerApi.updateComplaintStatus(
            complaint.getId(),
            new UpdateComplaintStatus().status(ComplaintStatus.RESOLVED).comment("Corrigé."));
    assertEquals(ComplaintStatus.RESOLVED, resolved.getStatus());

    var history = managerApi.getComplaintStatusHistory(complaint.getId(), null, null, null, null);
    assertEquals(3, history.size());
  }

  @Test
  void manager_reject_complaint_without_comment_ko() throws ApiException {
    var studentApi = new ComplaintApi(anApiClient(axelToken));
    var complaint =
        studentApi.createComplaint(
            new CrupdateComplaint()
                .type(ComplaintType.ACADEMIC)
                .title("Titre")
                .description("Description"));

    var managerApi = new ComplaintApi(anApiClient(managerToken));
    assertBadRequestException(
        "A reason is required to reject a complaint",
        () ->
            managerApi.updateComplaintStatus(
                complaint.getId(), new UpdateComplaintStatus().status(ComplaintStatus.REJECTED)));
  }

  @Test
  void manager_reject_complaint_with_comment_ok() throws ApiException {
    var studentApi = new ComplaintApi(anApiClient(axelToken));
    var complaint =
        studentApi.createComplaint(
            new CrupdateComplaint()
                .type(ComplaintType.ACADEMIC)
                .title("Titre")
                .description("Description"));

    var managerApi = new ComplaintApi(anApiClient(managerToken));
    var rejected =
        managerApi.updateComplaintStatus(
            complaint.getId(),
            new UpdateComplaintStatus().status(ComplaintStatus.REJECTED).comment("Non fondée."));

    assertEquals(ComplaintStatus.REJECTED, rejected.getStatus());
  }

  @Test
  void manager_cannot_reject_from_in_progress_ko() throws ApiException {
    var studentApi = new ComplaintApi(anApiClient(axelToken));
    var complaint =
        studentApi.createComplaint(
            new CrupdateComplaint()
                .type(ComplaintType.ACADEMIC)
                .title("Titre")
                .description("Description"));

    var managerApi = new ComplaintApi(anApiClient(managerToken));
    managerApi.updateComplaintStatus(
        complaint.getId(), new UpdateComplaintStatus().status(ComplaintStatus.IN_PROGRESS));

    assertBadRequestException(
        "Cannot move a complaint from IN_PROGRESS to REJECTED",
        () ->
            managerApi.updateComplaintStatus(
                complaint.getId(),
                new UpdateComplaintStatus()
                    .status(ComplaintStatus.REJECTED)
                    .comment("Trop tard.")));
  }

  @Test
  void student_get_own_complaints_list_ok() throws ApiException {
    var axelApi = new ComplaintApi(anApiClient(axelToken));
    var complaint =
        axelApi.createComplaint(
            new CrupdateComplaint()
                .type(ComplaintType.ACADEMIC)
                .title("Titre")
                .description("Description"));

    var ownComplaints = axelApi.getStudentComplaints(axel.getId(), null, null);
    assertTrue(ownComplaints.stream().anyMatch(c -> c.getId().equals(complaint.getId())));

    var tolotraApi = new ComplaintApi(anApiClient(tolotraToken));
    assertThrowsForbiddenException(() -> tolotraApi.getStudentComplaints(axel.getId(), null, null));
  }

  @Test
  void admin_create_complaint_routing_and_read_it_ok() throws ApiException {
    var adminApi = new ComplaintApi(anApiClient(adminToken));

    var routing =
        adminApi.createComplaintRouting(
            new CrupdateComplaintRouting().type(ComplaintType.FINANCIAL).managerId(admin.getId()));
    assertEquals(ComplaintType.FINANCIAL, routing.getType());
    assertEquals(admin.getId(), routing.getManager().getId());

    var last = adminApi.getLastComplaintRouting(ComplaintType.FINANCIAL);
    assertEquals(routing.getManager().getId(), last.getManager().getId());

    var routings = adminApi.getComplaintRoutings(null, null, List.of(ComplaintType.FINANCIAL));
    assertTrue(routings.stream().anyMatch(r -> r.getId().equals(routing.getId())));
  }

  @Test
  void manager_cannot_create_complaint_routing_ko() {
    var managerApi = new ComplaintApi(anApiClient(managerToken));
    assertThrowsForbiddenException(
        () ->
            managerApi.createComplaintRouting(
                new CrupdateComplaintRouting()
                    .type(ComplaintType.FINANCIAL)
                    .managerId(manager.getId())));
  }

  @Test
  void student_create_complaint_without_title_ko() {
    var api = new ComplaintApi(anApiClient(axelToken));
    var dto = new CrupdateComplaint().type(ComplaintType.ACADEMIC).description("Description");

    assertBadRequestException("Title is mandatory", () -> api.createComplaint(dto));
  }

  @Test
  void student_create_complaint_without_description_ko() {
    var api = new ComplaintApi(anApiClient(axelToken));
    var dto = new CrupdateComplaint().type(ComplaintType.ACADEMIC).title("Titre");

    assertBadRequestException("Description is mandatory", () -> api.createComplaint(dto));
  }

  @Test
  void manager_add_assignees_without_user_ids_ko() throws ApiException {
    var studentApi = new ComplaintApi(anApiClient(axelToken));
    var complaint =
        studentApi.createComplaint(
            new CrupdateComplaint()
                .type(ComplaintType.ACADEMIC)
                .title("Titre")
                .description("Description"));

    var managerApi = new ComplaintApi(anApiClient(managerToken));
    assertBadRequestException(
        "At least one user id is mandatory",
        () ->
            managerApi.addComplaintAssignees(
                complaint.getId(), new UpdateComplaintAssignees().userIds(List.of())));
  }

  @Test
  void manager_add_nonexistent_assignee_ko() throws ApiException {
    var studentApi = new ComplaintApi(anApiClient(axelToken));
    var complaint =
        studentApi.createComplaint(
            new CrupdateComplaint()
                .type(ComplaintType.ACADEMIC)
                .title("Titre")
                .description("Description"));

    var managerApi = new ComplaintApi(anApiClient(managerToken));
    assertThrowsApiException(
        "{\"type\":\"404 NOT_FOUND\",\"message\":\"User with id #unknown-user not found\"}",
        () ->
            managerApi.addComplaintAssignees(
                complaint.getId(),
                new UpdateComplaintAssignees().userIds(List.of("unknown-user"))));
  }

  @Test
  void manager_update_status_without_status_ko() throws ApiException {
    var studentApi = new ComplaintApi(anApiClient(axelToken));
    var complaint =
        studentApi.createComplaint(
            new CrupdateComplaint()
                .type(ComplaintType.ACADEMIC)
                .title("Titre")
                .description("Description"));

    var managerApi = new ComplaintApi(anApiClient(managerToken));
    assertBadRequestException(
        "Status is mandatory",
        () -> managerApi.updateComplaintStatus(complaint.getId(), new UpdateComplaintStatus()));
  }

  @Test
  void student_get_own_complaint_history_ok() throws ApiException {
    var studentApi = new ComplaintApi(anApiClient(axelToken));
    var complaint =
        studentApi.createComplaint(
            new CrupdateComplaint()
                .type(ComplaintType.ACADEMIC)
                .title("Titre")
                .description("Description"));

    var history = studentApi.getComplaintStatusHistory(complaint.getId(), null, null, null, null);

    assertEquals(1, history.size());
    assertEquals(ComplaintStatus.CREATED, history.getFirst().getStatus());
  }

  @Test
  void student_get_other_student_complaint_history_ko() throws ApiException {
    var axelApi = new ComplaintApi(anApiClient(axelToken));
    var complaint =
        axelApi.createComplaint(
            new CrupdateComplaint()
                .type(ComplaintType.ACADEMIC)
                .title("Titre")
                .description("Description"));

    var tolotraApi = new ComplaintApi(anApiClient(tolotraToken));
    assertThrowsForbiddenException(
        () -> tolotraApi.getComplaintStatusHistory(complaint.getId(), null, null, null, null));
  }

  @Test
  void student_upload_image_attachment_ok() throws ApiException, IOException, InterruptedException {
    var studentApi = new ComplaintApi(anApiClient(axelToken));
    var complaint =
        studentApi.createComplaint(
            new CrupdateComplaint()
                .type(ComplaintType.ACADEMIC)
                .title("Titre")
                .description("Description"));

    var response =
        uploadComplaintAttachment(
            localPort, axelToken, complaint.getId(), "capture-decran", "img", ".png", "image/png");
    var attachment = objectMapper.readValue(response.body(), ComplaintAttachment.class);

    assertEquals(HttpStatus.OK.value(), response.statusCode());
    assertNotNull(attachment.getId());
    assertEquals("capture-decran", attachment.getFileName());
    assertEquals("image/png", attachment.getContentType());
    assertNotNull(attachment.getFileUrl());
    assertEquals(axel.getId(), attachment.getUploadedBy().getId());

    var attachments = studentApi.getComplaintAttachments(complaint.getId());
    assertEquals(1, attachments.size());
    assertEquals(attachment.getId(), attachments.getFirst().getId());
  }

  @Test
  void student_upload_pdf_attachment_ok() throws ApiException, IOException, InterruptedException {
    var studentApi = new ComplaintApi(anApiClient(axelToken));
    var complaint =
        studentApi.createComplaint(
            new CrupdateComplaint()
                .type(ComplaintType.ACADEMIC)
                .title("Titre")
                .description("Description"));

    var response =
        uploadComplaintAttachment(
            localPort,
            axelToken,
            complaint.getId(),
            "justificatif",
            "test",
            ".pdf",
            "application/pdf");
    var attachment = objectMapper.readValue(response.body(), ComplaintAttachment.class);

    assertEquals(HttpStatus.OK.value(), response.statusCode());
    assertEquals("application/pdf", attachment.getContentType());
  }

  @Test
  void upload_attachment_rejects_disallowed_file_type_ko()
      throws ApiException, IOException, InterruptedException {
    var studentApi = new ComplaintApi(anApiClient(axelToken));
    var complaint =
        studentApi.createComplaint(
            new CrupdateComplaint()
                .type(ComplaintType.ACADEMIC)
                .title("Titre")
                .description("Description"));

    var response =
        uploadComplaintAttachment(
            localPort,
            axelToken,
            complaint.getId(),
            "disguised",
            "dummy-excel",
            ".xlsx",
            "image/png");

    assertEquals(HttpStatus.BAD_REQUEST.value(), response.statusCode());
  }

  @Test
  void student_cannot_upload_attachment_to_other_student_complaint_ko() throws ApiException {
    var studentApi = new ComplaintApi(anApiClient(axelToken));
    var complaint =
        studentApi.createComplaint(
            new CrupdateComplaint()
                .type(ComplaintType.ACADEMIC)
                .title("Titre")
                .description("Description"));

    var tolotraApi = new ComplaintApi(anApiClient(tolotraToken));
    var imgFile = TestFiles.getMockedFile("test", ".pdf");
    assertThrowsForbiddenException(
        () -> tolotraApi.uploadComplaintAttachment(complaint.getId(), "intrusion", imgFile));
    assertThrowsForbiddenException(() -> tolotraApi.getComplaintAttachments(complaint.getId()));
  }

  @Test
  void teacher_assigned_can_upload_and_list_attachments_ok()
      throws ApiException, IOException, InterruptedException {
    var studentApi = new ComplaintApi(anApiClient(axelToken));
    var complaint =
        studentApi.createComplaint(
            new CrupdateComplaint()
                .type(ComplaintType.ACADEMIC)
                .title("Titre")
                .description("Description"));

    var managerApi = new ComplaintApi(anApiClient(managerToken));
    managerApi.addComplaintAssignees(
        complaint.getId(), new UpdateComplaintAssignees().userIds(List.of(teacher.getId())));

    var response =
        uploadComplaintAttachment(
            localPort, teacherToken, complaint.getId(), "reponse", "img", ".png", "image/png");
    assertEquals(HttpStatus.OK.value(), response.statusCode());

    var teacherApi = new ComplaintApi(anApiClient(teacherToken));
    var attachments = teacherApi.getComplaintAttachments(complaint.getId());
    assertEquals(1, attachments.size());
  }

  @Test
  void teacher_cannot_upload_attachment_to_unassigned_complaint_ko() throws ApiException {
    var studentApi = new ComplaintApi(anApiClient(axelToken));
    var complaint =
        studentApi.createComplaint(
            new CrupdateComplaint()
                .type(ComplaintType.ACADEMIC)
                .title("Titre")
                .description("Description"));

    var teacherApi = new ComplaintApi(anApiClient(teacherToken));
    var imgFile = TestFiles.getMockedFile("test", ".pdf");
    assertThrowsForbiddenException(
        () -> teacherApi.uploadComplaintAttachment(complaint.getId(), "intrusion", imgFile));
  }

  @Test
  void monitor_create_complaint_for_linked_student_ok() throws ApiException {
    var monitorApi = new ComplaintApi(anApiClient(monitorOfAxelToken));
    var dto =
        new CrupdateComplaint()
            .type(ComplaintType.ACADEMIC)
            .title("Facture erronée")
            .description("Le montant facturé ne correspond pas à l'échéancier.")
            .studentId(axel.getId());

    var created = monitorApi.createComplaint(dto);

    assertEquals(axel.getId(), created.getStudent().getId());
    assertEquals(monitorOfAxel.getId(), created.getCreatedBy().getId());
  }

  @Test
  void monitor_create_complaint_without_student_id_ko() {
    var monitorApi = new ComplaintApi(anApiClient(monitorOfAxelToken));
    var dto =
        new CrupdateComplaint()
            .type(ComplaintType.FINANCIAL)
            .title("Titre")
            .description("Description");

    assertBadRequestException(
        "student_id is mandatory when a monitor creates a complaint",
        () -> monitorApi.createComplaint(dto));
  }

  @Test
  void monitor_cannot_create_complaint_for_unlinked_student_ko() {
    var monitorApi = new ComplaintApi(anApiClient(unlinkedMonitorToken));
    var dto =
        new CrupdateComplaint()
            .type(ComplaintType.FINANCIAL)
            .title("Titre")
            .description("Description")
            .studentId(axel.getId());

    var exception = assertThrows(ApiException.class, () -> monitorApi.createComplaint(dto));
    assertEquals(HttpStatus.FORBIDDEN.value(), exception.getCode());
  }

  @Test
  void monitor_of_linked_student_can_read_complaint_fully_ok() throws ApiException {
    var studentApi = new ComplaintApi(anApiClient(axelToken));
    var complaint =
        studentApi.createComplaint(
            new CrupdateComplaint()
                .type(ComplaintType.ACADEMIC)
                .title("Titre")
                .description("Description"));

    var monitorApi = new ComplaintApi(anApiClient(monitorOfAxelToken));
    var fetched = monitorApi.getComplaintById(complaint.getId());
    assertEquals(complaint.getId(), fetched.getId());

    var history = monitorApi.getComplaintStatusHistory(complaint.getId(), null, null, null, null);
    assertEquals(1, history.size());

    var attachments = monitorApi.getComplaintAttachments(complaint.getId());
    assertEquals(0, attachments.size());

    var ownComplaints = monitorApi.getStudentComplaints(axel.getId(), null, null);
    assertTrue(ownComplaints.stream().anyMatch(c -> c.getId().equals(complaint.getId())));
  }

  @Test
  void unlinked_monitor_cannot_read_complaint_ko() throws ApiException {
    var studentApi = new ComplaintApi(anApiClient(axelToken));
    var complaint =
        studentApi.createComplaint(
            new CrupdateComplaint()
                .type(ComplaintType.ACADEMIC)
                .title("Titre")
                .description("Description"));

    var monitorApi = new ComplaintApi(anApiClient(unlinkedMonitorToken));
    assertThrowsForbiddenException(() -> monitorApi.getComplaintById(complaint.getId()));
    assertThrowsForbiddenException(
        () -> monitorApi.getComplaintStatusHistory(complaint.getId(), null, null, null, null));
    assertThrowsForbiddenException(() -> monitorApi.getComplaintAttachments(complaint.getId()));
    assertThrowsForbiddenException(() -> monitorApi.getStudentComplaints(axel.getId(), null, null));
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }
}
