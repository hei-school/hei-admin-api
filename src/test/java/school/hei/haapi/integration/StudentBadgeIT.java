package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.PRESENT;
import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.UNCHECKED;
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
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

class StudentBadgeIT extends FacadeITMockedThirdParties {
  @MockBean private EventBridgeClient eventBridgeClientMock;

  @Autowired private UserRepository userRepository;
  @Autowired private GroupRepository groupRepository;
  @Autowired private GroupFlowRepository groupFlowRepository;
  @Autowired private EventRepository eventRepository;
  @Autowired private EventParticipantRepository eventParticipantRepository;
  @Autowired private StudentBadgeRepository studentBadgeRepository;

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
    // the manager prints the badges of the group: a public id is created for the student
    HttpResponse<String> pdf =
        send("GET", "/students/badges/raw?group_id=" + group.getId(), managerToken);
    assertEquals(200, pdf.statusCode());
    assertTrue(pdf.body().startsWith("%PDF"));
    String publicId = activePublicId();
    assertNotEquals(student.getId(), publicId);

    // anyone scanning the QR code sees public information, never the real id
    HttpResponse<String> publicStudent = send("GET", "/students/public/" + publicId, null);
    assertEquals(200, publicStudent.statusCode());
    assertTrue(publicStudent.body().contains(student.getRef()));
    assertTrue(publicStudent.body().contains("\"is_valid\":true"));
    assertFalse(publicStudent.body().contains(student.getId()));

    // connected staff get the real student, to open its profile with its fees
    HttpResponse<String> fullStudent =
        send("GET", "/students/public/" + publicId + "/student", managerToken);
    assertEquals(200, fullStudent.statusCode());
    assertTrue(fullStudent.body().contains(student.getId()));
    assertEquals(
        403, send("GET", "/students/public/" + publicId + "/student", studentToken).statusCode());

    // the teacher scans the badge from the event page: the student is present
    String attendancePath =
        "/events/" + event.getId() + "/students/public/" + publicId + "/attendance";
    assertEquals(403, send("PUT", attendancePath, studentToken).statusCode());
    assertEquals(403, send("PUT", attendancePath, managerToken).statusCode());
    assertEquals(200, send("PUT", attendancePath, teacherToken).statusCode());
    assertEquals(
        PRESENT,
        eventParticipantRepository.findById(participant.getId()).orElseThrow().getStatus());

    // no second badge in circulation while this one is active
    assertEquals(
        400,
        send("GET", "/students/badges/raw?group_id=" + group.getId(), managerToken).statusCode());
    assertEquals(publicId, activePublicId());

    // a lost badge is revoked: it cannot check attendance anymore
    assertEquals(
        200,
        send("PUT", "/students/public/" + publicId + "/revocation", managerToken).statusCode());
    assertTrue(
        send("GET", "/students/public/" + publicId, null).body().contains("\"is_valid\":false"));
    assertEquals(400, send("PUT", attendancePath, teacherToken).statusCode());
  }

  @Test
  void print_and_remove_badge_of_one_student() throws Exception {
    String badgePath = "/students/" + student.getId() + "/badge";
    assertEquals(404, send("GET", badgePath, managerToken).statusCode());

    // badge of this student only, from its profile
    HttpResponse<String> pdf =
        send("GET", "/students/badges/raw?student_ids=" + student.getId(), managerToken);
    assertEquals(200, pdf.statusCode());
    HttpResponse<String> activeBadge = send("GET", badgePath, managerToken);
    assertEquals(200, activeBadge.statusCode());
    assertEquals(403, send("GET", badgePath, teacherToken).statusCode());
    String firstPublicId = activePublicId();
    assertTrue(activeBadge.body().contains(firstPublicId));
    assertEquals(
        400,
        send("GET", "/students/badges/raw?student_ids=" + student.getId(), managerToken)
            .statusCode());

    // removing the badge: its QR code stops working, a new one can be printed
    assertEquals(403, send("PUT", badgePath + "/revocation", teacherToken).statusCode());
    assertEquals(200, send("PUT", badgePath + "/revocation", managerToken).statusCode());
    assertEquals(404, send("GET", badgePath, managerToken).statusCode());
    assertTrue(
        send("GET", "/students/public/" + firstPublicId, null)
            .body()
            .contains("\"is_valid\":false"));
    send("GET", "/students/badges/raw?student_ids=" + student.getId(), managerToken);
    assertNotEquals(firstPublicId, activePublicId());
  }

  @Test
  void badge_of_a_past_academic_year_has_expired() throws Exception {
    // a badge printed for 2024 - 2025: its year is over
    AcademicYear pastYear = AcademicYear.parse("2024 - 2025");
    String expiredPublicId =
        studentBadgeRepository
            .save(
                StudentBadge.builder()
                    .student(student)
                    .publicId(java.util.UUID.randomUUID().toString())
                    .academicYear(pastYear.label())
                    .expirationDatetime(pastYear.badgeExpiration())
                    .build())
            .getPublicId();

    assertTrue(
        send("GET", "/students/public/" + expiredPublicId, null)
            .body()
            .contains("\"is_valid\":false"));
    assertEquals(
        400,
        send(
                "PUT",
                "/events/" + event.getId() + "/students/public/" + expiredPublicId + "/attendance",
                teacherToken)
            .statusCode());
    assertEquals(
        404, send("GET", "/students/" + student.getId() + "/badge", managerToken).statusCode());

    // printing the current academic year gives a new badge with a new QR code
    send("GET", "/students/badges/raw?student_ids=" + student.getId(), managerToken);
    assertNotEquals(expiredPublicId, activePublicId());
  }

  @Test
  void unknown_public_id_is_not_found() throws Exception {
    assertEquals(
        404,
        send("GET", "/students/public/7c1e4b9a-2f3d-4e8a-9b6c-1d2e3f4a5b6c", null).statusCode());
  }

  private String activePublicId() {
    return studentBadgeRepository
        .findFirstByStudentIdAndRevocationDatetimeIsNullAndExpirationDatetimeAfterOrderByExpirationDatetimeDesc(
            student.getId(), Instant.now())
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
