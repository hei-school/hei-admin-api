package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static school.hei.haapi.integration.conf.ApiAssertions.assertBadRequestException;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsApiException;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.FakeDataProvider.someStudent;
import static school.hei.haapi.integration.conf.FakeDataProvider.someUser;
import static school.hei.haapi.integration.conf.TestAuth.tokenFor;
import static school.hei.haapi.integration.conf.TestFiles.uploadComplaintAttachment;
import static school.hei.haapi.model.User.Role.ADMIN;
import static school.hei.haapi.model.User.Role.MANAGER;
import static school.hei.haapi.model.User.Role.TEACHER;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.ComplaintRoutingService;

class ComplaintIT extends FacadeITMockedThirdParties {
  @Autowired private UserRepository userRepository;
  @Autowired private ComplaintRoutingService complaintRoutingService;
  @Autowired private ObjectMapper objectMapper;
  @MockBean private EventProducer<ComplaintEmailNotificationRequested> emailEventProducerMock;
  @MockBean private EventProducer<ComplaintSmsNotificationRequested> smsEventProducerMock;

  private User axel;
  private User tolotra;
  private User manager;
  private User admin;
  private User teacher;
  private User otherTeacher;
  private String axelToken;
  private String tolotraToken;
  private String managerToken;
  private String adminToken;
  private String teacherToken;
  private String otherTeacherToken;

  @BeforeEach
  void setUp() {
    axel = userRepository.save(someStudent("axel"));
    tolotra = userRepository.save(someStudent("tolotra"));
    manager = userRepository.save(someUser("manager", MANAGER));
    admin = userRepository.save(someUser("admin", ADMIN));
    teacher = userRepository.save(someUser("teacher", TEACHER));
    otherTeacher = userRepository.save(someUser("other-teacher", TEACHER));

    complaintRoutingService.create(school.hei.haapi.model.ComplaintType.ACADEMIC, manager);

    axelToken = tokenFor(casdoorAuthServiceMock, axel);
    tolotraToken = tokenFor(casdoorAuthServiceMock, tolotra);
    managerToken = tokenFor(casdoorAuthServiceMock, manager);
    adminToken = tokenFor(casdoorAuthServiceMock, admin);
    teacherToken = tokenFor(casdoorAuthServiceMock, teacher);
    otherTeacherToken = tokenFor(casdoorAuthServiceMock, otherTeacher);
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
    // one email on creation (to the routed manager), two on assignment (new assignee + student)
    verify(emailEventProducerMock, times(3)).accept(anyList());
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
  void student_upload_and_list_attachment_ok()
      throws ApiException, IOException, InterruptedException {
    when(fileService.getPresignedUrl(anyString(), anyLong()))
        .thenReturn("https://s3.example.com/preuve");
    var studentApi = new ComplaintApi(anApiClient(axelToken));
    var complaint =
        studentApi.createComplaint(
            new CrupdateComplaint()
                .type(ComplaintType.ACADEMIC)
                .title("Titre")
                .description("Description"));

    var response = uploadComplaintAttachment(localPort, axelToken, complaint.getId(), "preuve");
    assertEquals(200, response.statusCode());
    var uploaded = objectMapper.readValue(response.body(), ComplaintAttachment.class);
    assertEquals("preuve", uploaded.getFileName());
    assertNotNull(uploaded.getFileUrl());

    var managerApi = new ComplaintApi(anApiClient(managerToken));
    var attachments = managerApi.getComplaintAttachments(complaint.getId());
    assertEquals(1, attachments.size());
    assertEquals(uploaded.getId(), attachments.getFirst().getId());

    var fetched = managerApi.getComplaintById(complaint.getId());
    assertEquals(1, fetched.getAttachments().size());
  }

  @Test
  void student_upload_attachment_to_other_student_complaint_ko()
      throws ApiException, IOException, InterruptedException {
    var axelApi = new ComplaintApi(anApiClient(axelToken));
    var complaint =
        axelApi.createComplaint(
            new CrupdateComplaint()
                .type(ComplaintType.ACADEMIC)
                .title("Titre")
                .description("Description"));

    var response = uploadComplaintAttachment(localPort, tolotraToken, complaint.getId(), "preuve");
    assertEquals(403, response.statusCode());
  }

  @Test
  void assigned_teacher_can_read_and_progress_complaint_ok() throws ApiException {
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

    var history = teacherApi.getComplaintStatusHistory(complaint.getId(), null, null, null, null);
    assertTrue(history.size() >= 1);

    var inProgress =
        teacherApi.updateComplaintStatus(
            complaint.getId(), new UpdateComplaintStatus().status(ComplaintStatus.IN_PROGRESS));
    assertEquals(ComplaintStatus.IN_PROGRESS, inProgress.getStatus());
  }

  @Test
  void unassigned_teacher_cannot_read_complaint_ko() throws ApiException {
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

    var otherTeacherApi = new ComplaintApi(anApiClient(otherTeacherToken));
    assertThrowsForbiddenException(() -> otherTeacherApi.getComplaintById(complaint.getId()));
    assertThrowsForbiddenException(
        () ->
            otherTeacherApi.updateComplaintStatus(
                complaint.getId(),
                new UpdateComplaintStatus().status(ComplaintStatus.IN_PROGRESS)));
  }

  @Test
  void teacher_list_complaints_is_scoped_to_own_assignments_ok() throws ApiException {
    var studentApi = new ComplaintApi(anApiClient(axelToken));
    var ownComplaint =
        studentApi.createComplaint(
            new CrupdateComplaint()
                .type(ComplaintType.ACADEMIC)
                .title("Titre")
                .description("Description"));
    var otherComplaint =
        studentApi.createComplaint(
            new CrupdateComplaint()
                .type(ComplaintType.ACADEMIC)
                .title("Autre titre")
                .description("Autre description"));

    var managerApi = new ComplaintApi(anApiClient(managerToken));
    managerApi.addComplaintAssignees(
        ownComplaint.getId(), new UpdateComplaintAssignees().userIds(List.of(teacher.getId())));
    managerApi.addComplaintAssignees(
        otherComplaint.getId(),
        new UpdateComplaintAssignees().userIds(List.of(otherTeacher.getId())));

    var teacherApi = new ComplaintApi(anApiClient(teacherToken));
    var visible = teacherApi.getComplaints(null, null, null, null, null, null, null, null);

    assertTrue(
        visible.stream()
            .allMatch(
                c -> c.getAssignees().stream().anyMatch(a -> a.getId().equals(teacher.getId()))));
    assertTrue(visible.stream().anyMatch(c -> c.getId().equals(ownComplaint.getId())));
    assertTrue(visible.stream().noneMatch(c -> c.getId().equals(otherComplaint.getId())));
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }
}
