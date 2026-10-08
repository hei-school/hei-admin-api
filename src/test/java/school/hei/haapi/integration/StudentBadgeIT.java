package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.PRESENT;
import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.UNCHECKED;
import static school.hei.haapi.endpoint.rest.model.BadgeInvalidity.EXPIRED;
import static school.hei.haapi.endpoint.rest.model.BadgeInvalidity.REVOKED;
import static school.hei.haapi.endpoint.rest.model.EventType.COURSE;
import static school.hei.haapi.integration.conf.TestAuth.tokenFor;
import static school.hei.haapi.integration.conf.TestMocks.setUpEventBridge;
import static school.hei.haapi.integration.testData.EventTestData.aParticipant;
import static school.hei.haapi.integration.testData.EventTestData.anEvent;
import static school.hei.haapi.integration.testData.GroupTestData.createGroupFlow;
import static school.hei.haapi.integration.testData.GroupTestData.g1;
import static school.hei.haapi.integration.testData.ManagerTestData.hasina;
import static school.hei.haapi.integration.testData.StudentTestData.axel;
import static school.hei.haapi.integration.testData.TeacherTestData.toky;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.rest.model.EncryptedBadge;
import school.hei.haapi.endpoint.rest.model.PublicStudent;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.StudentBadge;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.EventParticipantRepository;
import school.hei.haapi.repository.EventRepository;
import school.hei.haapi.repository.GroupFlowRepository;
import school.hei.haapi.repository.GroupRepository;
import school.hei.haapi.repository.StudentBadgeRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.utils.AcademicYear;
import school.hei.haapi.service.utils.BadgeCipher;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

class StudentBadgeIT extends FacadeITMockedThirdParties {
  @MockBean private EventBridgeClient eventBridgeClientMock;

  @Autowired private UserRepository userRepository;
  @Autowired private GroupRepository groupRepository;
  @Autowired private GroupFlowRepository groupFlowRepository;
  @Autowired private EventRepository eventRepository;
  @Autowired private EventParticipantRepository eventParticipantRepository;
  @Autowired private StudentBadgeRepository studentBadgeRepository;
  @Autowired private BadgeCipher badgeCipher;
  @Autowired private ObjectMapper objectMapper;

  private User student;
  private User teacher;
  private User manager;
  private Group group;
  private GroupFlow groupFlow;
  private Event event;
  private EventParticipant participant;
  private String studentToken;
  private String teacherToken;
  private String managerToken;

  @BeforeEach
  void setUp() {
    setUpEventBridge(eventBridgeClientMock);
    student = userRepository.save(axel());
    teacher = userRepository.save(toky());
    manager = userRepository.save(hasina());
    group = groupRepository.save(g1());
    groupFlow = groupFlowRepository.save(createGroupFlow(student, group));
    Instant now = Instant.now();
    event =
        eventRepository.save(
            anEvent(
                teacher,
                COURSE,
                "Badge scan",
                now.minus(1, ChronoUnit.HOURS),
                now.plus(1, ChronoUnit.HOURS)));
    participant = eventParticipantRepository.save(aParticipant(event, student, group, UNCHECKED));

    studentToken = tokenFor(casdoorAuthServiceMock, student);
    teacherToken = tokenFor(casdoorAuthServiceMock, teacher);
    managerToken = tokenFor(casdoorAuthServiceMock, manager);
  }

  @AfterEach
  void tearDown() {
    studentBadgeRepository.deleteAll(
        studentBadgeRepository.findAll().stream()
            .filter(badge -> badge.getStudent().getId().equals(student.getId()))
            .toList());
    eventParticipantRepository.deleteById(participant.getId());
    eventRepository.deleteById(event.getId());
    groupFlowRepository.deleteById(groupFlow.getId());
    groupRepository.deleteById(group.getId());
    userRepository.deleteAllById(
        java.util.List.of(student.getId(), teacher.getId(), manager.getId()));
  }

  @Test
  void print_then_scan_badge() throws Exception {
    var pdf = send("GET", "/students/badges/raw?group_id=" + group.getId(), managerToken);
    assertEquals(200, pdf.statusCode());
    assertTrue(pdf.body().startsWith("%PDF"));
    String publicId = activePublicId();
    assertNotEquals(student.getId(), publicId);

    var publicStudent = send("GET", "/badges/" + publicId, null);
    assertEquals(200, publicStudent.statusCode());
    assertFalse(publicStudent.body().contains(student.getRef()));
    assertFalse(publicStudent.body().contains(student.getId()));
    var decrypted = publicStudentOf(publicStudent, publicId);
    assertEquals(student.getRef(), decrypted.getRef());
    assertEquals(true, decrypted.getIsValid());
    assertEquals(java.util.List.of(), decrypted.getLateFees());

    var ownerPath = "/badges/" + publicId + "/student";
    var owner = send("GET", ownerPath, managerToken);
    assertEquals(200, owner.statusCode());
    assertEquals("{\"id\":\"" + student.getId() + "\"}", owner.body());
    assertEquals(403, send("GET", ownerPath, teacherToken).statusCode());
    assertEquals(403, send("GET", ownerPath, studentToken).statusCode());

    // the event is not a course of the teacher: opening the badge marks nothing
    var autoAttendancePath = "/badges/" + publicId + "/attendance";
    var autoAttendance = send("PUT", autoAttendancePath, teacherToken);
    assertEquals(200, autoAttendance.statusCode());
    assertTrue(autoAttendance.body().contains("\"result\":\"NO_COURSE_IN_PROGRESS\""));
    assertEquals(403, send("PUT", autoAttendancePath, studentToken).statusCode());
    assertEquals(403, send("PUT", autoAttendancePath, managerToken).statusCode());
    assertEquals(
        UNCHECKED,
        eventParticipantRepository.findById(participant.getId()).orElseThrow().getStatus());

    var attendancePath = "/badges/" + publicId + "/events/" + event.getId() + "/attendance";
    assertEquals(403, send("PUT", attendancePath, studentToken).statusCode());
    assertEquals(403, send("PUT", attendancePath, managerToken).statusCode());
    assertEquals(200, send("PUT", attendancePath, teacherToken).statusCode());
    assertEquals(
        PRESENT,
        eventParticipantRepository.findById(participant.getId()).orElseThrow().getStatus());

    assertEquals(
        400,
        send("GET", "/students/badges/raw?group_id=" + group.getId(), managerToken).statusCode());
    assertEquals(publicId, activePublicId());

    assertEquals(
        403, send("PUT", "/badges/" + publicId + "/revocation", teacherToken).statusCode());
    assertEquals(
        200, send("PUT", "/badges/" + publicId + "/revocation", managerToken).statusCode());
    var revoked = publicStudentOf(send("GET", "/badges/" + publicId, null), publicId);
    assertEquals(false, revoked.getIsValid());
    assertEquals(REVOKED, revoked.getInvalidity());
    assertNull(revoked.getRef());
    assertNull(revoked.getLastName());
    assertEquals(400, send("PUT", attendancePath, teacherToken).statusCode());
    assertEquals(400, send("PUT", autoAttendancePath, teacherToken).statusCode());
  }

  @Test
  void print_and_remove_badge_of_one_student() throws Exception {
    var badgePath = "/students/" + student.getId() + "/badge";
    assertEquals(404, send("GET", badgePath, managerToken).statusCode());

    var pdf = send("GET", "/students/badges/raw?student_ids=" + student.getId(), managerToken);
    assertEquals(200, pdf.statusCode());
    var activeBadge = send("GET", badgePath, managerToken);
    assertEquals(200, activeBadge.statusCode());
    assertEquals(403, send("GET", badgePath, teacherToken).statusCode());
    var firstPublicId = activePublicId();
    assertTrue(activeBadge.body().contains(firstPublicId));
    assertEquals(
        400,
        send("GET", "/students/badges/raw?student_ids=" + student.getId(), managerToken)
            .statusCode());

    assertEquals(403, send("PUT", badgePath + "/revocation", teacherToken).statusCode());
    assertEquals(200, send("PUT", badgePath + "/revocation", managerToken).statusCode());
    assertEquals(404, send("GET", badgePath, managerToken).statusCode());
    assertEquals(
        false,
        publicStudentOf(send("GET", "/badges/" + firstPublicId, null), firstPublicId).getIsValid());
    send("GET", "/students/badges/raw?student_ids=" + student.getId(), managerToken);
    assertNotEquals(firstPublicId, activePublicId());
  }

  @Test
  void badge_of_a_past_academic_year_has_expired() throws Exception {
    var pastYear = AcademicYear.parse("2024 - 2025");
    var expiredPublicId =
        studentBadgeRepository
            .save(
                StudentBadge.builder()
                    .student(student)
                    .publicId(java.util.UUID.randomUUID().toString())
                    .academicYear(pastYear.label())
                    .expirationDatetime(pastYear.badgeExpiration())
                    .build())
            .getPublicId();

    var expired = publicStudentOf(send("GET", "/badges/" + expiredPublicId, null), expiredPublicId);
    assertEquals(EXPIRED, expired.getInvalidity());
    assertNull(expired.getRef());
    assertEquals(
        400,
        send(
                "PUT",
                "/badges/" + expiredPublicId + "/events/" + event.getId() + "/attendance",
                teacherToken)
            .statusCode());
    assertEquals(
        404, send("GET", "/students/" + student.getId() + "/badge", managerToken).statusCode());

    send("GET", "/students/badges/raw?student_ids=" + student.getId(), managerToken);
    assertNotEquals(expiredPublicId, activePublicId());
  }

  @Test
  void unknown_or_guessed_public_id_is_not_found() throws Exception {
    assertEquals(
        404, send("GET", "/badges/7c1e4b9a-2f3d-4e8a-9b6c-1d2e3f4a5b6c", null).statusCode());
    assertEquals(404, send("GET", "/badges/" + student.getId(), null).statusCode());
    assertEquals(404, send("GET", "/badges/raw", null).statusCode());
  }

  @Test
  void only_badge_public_ids_are_public_not_the_badges_pdf() throws Exception {
    var pdfWithoutToken =
        send("GET", "/students/badges/raw?group_id=" + group.getId(), null).statusCode();
    assertTrue(pdfWithoutToken == 401 || pdfWithoutToken == 403);
    assertEquals(
        403,
        send("GET", "/students/badges/raw?group_id=" + group.getId(), studentToken).statusCode());
  }

  private PublicStudent publicStudentOf(HttpResponse<String> response, String publicId)
      throws IOException {
    var encrypted = objectMapper.readValue(response.body(), EncryptedBadge.class);
    return badgeCipher.decrypt(encrypted, publicId, PublicStudent.class);
  }

  private String activePublicId() {
    return studentBadgeRepository
        .findCurrentBadgeOfStudent(student.getId(), Instant.now())
        .orElseThrow()
        .getPublicId();
  }

  private HttpResponse<String> send(String method, String path, String token)
      throws IOException, InterruptedException {
    HttpRequest.Builder request =
        HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + localPort + path))
            .method(method, HttpRequest.BodyPublishers.noBody());
    if (token != null) {
      request.header("Authorization", "Bearer " + token);
    }
    return HttpClient.newBuilder()
        .build()
        .send(request.build(), HttpResponse.BodyHandlers.ofString());
  }
}
