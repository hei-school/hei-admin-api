package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.*;
import static school.hei.haapi.endpoint.rest.model.ReactionEnum.CHECK;
import static school.hei.haapi.endpoint.rest.model.ReactionEnum.UNCHECK;
import static school.hei.haapi.endpoint.rest.model.Scope.GLOBAL;
import static school.hei.haapi.endpoint.rest.model.Scope.MANAGER;
import static school.hei.haapi.endpoint.rest.model.Scope.STUDENT;
import static school.hei.haapi.endpoint.rest.model.Scope.TEACHER;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestAuth.tokenFor;
import static school.hei.haapi.integration.conf.TestMocks.setUpS3Service;
import static school.hei.haapi.integration.testData.AnnouncementTestData.anAnnouncement;
import static school.hei.haapi.integration.testData.GroupTestData.createGroupFlow;
import static school.hei.haapi.integration.testData.GroupTestData.g1;
import static school.hei.haapi.integration.testData.ManagerTestData.hasina;
import static school.hei.haapi.integration.testData.MonitorTestData.monitorOfAxel;
import static school.hei.haapi.integration.testData.StaffTestData.adminMialy;
import static school.hei.haapi.integration.testData.StudentTestData.axel;
import static school.hei.haapi.integration.testData.TeacherTestData.toky;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.rest.api.AnnouncementsApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CreateAnnouncement;
import school.hei.haapi.endpoint.rest.model.GroupIdentifier;
import school.hei.haapi.endpoint.rest.model.ReactToAnnouncementRequest;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Announcement;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.AnnouncementRepository;
import school.hei.haapi.repository.GroupFlowRepository;
import school.hei.haapi.repository.GroupRepository;
import school.hei.haapi.repository.UserRepository;

public class AnnouncementIT extends FacadeITMockedThirdParties {
  private static final Instant BEFORE_WINDOW = Instant.parse("2022-12-20T08:00:00.00Z");
  private static final Instant IN_WINDOW_EARLY = Instant.parse("2022-12-21T08:00:00.00Z");
  private static final Instant IN_WINDOW_LATE = Instant.parse("2022-12-21T20:00:00.00Z");

  @MockBean EventProducer producer;
  @Autowired private UserRepository userRepository;
  @Autowired private GroupRepository groupRepository;
  @Autowired private GroupFlowRepository groupFlowRepository;
  @Autowired private AnnouncementRepository announcementRepository;

  private User managerHasina;
  private User teacherToky;
  private User studentAxel;
  private User monitorAxel;
  private User adminUser;
  private Group groupG1;
  private GroupFlow axelJoinsG1;

  private Announcement forAll;
  private Announcement forTeacher;
  private Announcement especiallyForG1;
  private Announcement forManager;

  private String managerToken;
  private String teacherToken;
  private String axelToken;
  private String monitorToken;
  private String adminToken;

  private void setUpTestData() {
    managerHasina = userRepository.save(hasina());
    teacherToky = userRepository.save(toky());
    studentAxel = userRepository.save(axel());
    adminUser = userRepository.save(adminMialy());

    monitorAxel = monitorOfAxel();
    monitorAxel.setMonitors(new ArrayList<>(List.of(studentAxel)));
    monitorAxel = userRepository.save(monitorAxel);

    groupG1 = groupRepository.save(g1());
    axelJoinsG1 = groupFlowRepository.save(createGroupFlow(studentAxel, groupG1));

    forAll = announcementRepository.save(anAnnouncement(managerHasina, GLOBAL, "Fermeture bureau"));
    forTeacher =
        announcementRepository.save(anAnnouncement(managerHasina, TEACHER, "Conge autorise"));
    especiallyForG1 =
        announcementRepository.save(
            anAnnouncement(teacherToky, STUDENT, "Cours annule G1", List.of(groupG1)));
    forManager =
        announcementRepository.save(anAnnouncement(managerHasina, MANAGER, "Comptabilite"));

    forAll.setCreationDatetime(BEFORE_WINDOW);
    forTeacher.setCreationDatetime(IN_WINDOW_EARLY);
    especiallyForG1.setCreationDatetime(IN_WINDOW_LATE);
    forManager.setCreationDatetime(BEFORE_WINDOW);
    announcementRepository.saveAll(List.of(forAll, forTeacher, especiallyForG1, forManager));
  }

  @BeforeEach
  void setUp() {
    setUpTestData();
    setUpS3Service(fileService, studentAxel);

    managerToken = tokenFor(casdoorAuthServiceMock, managerHasina);
    teacherToken = tokenFor(casdoorAuthServiceMock, teacherToky);
    axelToken = tokenFor(casdoorAuthServiceMock, studentAxel);
    monitorToken = tokenFor(casdoorAuthServiceMock, monitorAxel);
    adminToken = tokenFor(casdoorAuthServiceMock, adminUser);
  }

  @AfterEach
  void tearDown() {
    announcementRepository.deleteAllById(
        List.of(forAll.getId(), forTeacher.getId(), especiallyForG1.getId(), forManager.getId()));
    groupFlowRepository.deleteById(axelJoinsG1.getId());
    groupRepository.deleteById(groupG1.getId());
    monitorAxel.setMonitors(new ArrayList<>());
    userRepository.save(monitorAxel);
    userRepository.deleteAll(
        List.of(managerHasina, teacherToky, studentAxel, monitorAxel, adminUser));
  }

  private AnnouncementsApi apiAs(String token) {
    return new AnnouncementsApi(anApiClient(token));
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  /** The REST model shares its name with the entity, so it is named here and nowhere else. */
  private static List<String> idsOf(
      List<school.hei.haapi.endpoint.rest.model.Announcement> announcements) {
    return announcements.stream().map(announcement -> announcement.getId()).toList();
  }

  @Test
  void manager_read_announcements_ok() throws ApiException {
    var api = apiAs(managerToken);
    var all = api.getAnnouncements(1, 100, null, null, null, null);
    assertTrue(
        idsOf(all)
            .containsAll(
                List.of(
                    forAll.getId(),
                    especiallyForG1.getId(),
                    forManager.getId(),
                    forTeacher.getId())));

    var byAuthorRef = api.getAnnouncements(1, 100, null, null, managerHasina.getRef(), null);
    assertTrue(idsOf(byAuthorRef).contains(forAll.getId()));
    assertTrue(idsOf(byAuthorRef).contains(forTeacher.getId()));
    assertFalse(idsOf(byAuthorRef).contains(especiallyForG1.getId()));

    var byCreationDatetime =
        api.getAnnouncements(
            1, 100, IN_WINDOW_EARLY, Instant.parse("2022-12-22T08:00:00.00Z"), null, null);
    assertFalse(idsOf(byCreationDatetime).contains(forAll.getId()));
    assertTrue(idsOf(byCreationDatetime).contains(forTeacher.getId()));
    assertTrue(idsOf(byCreationDatetime).contains(especiallyForG1.getId()));

    var byScope = api.getAnnouncements(1, 100, null, null, null, TEACHER);
    assertTrue(idsOf(byScope).contains(forTeacher.getId()));
    assertFalse(idsOf(byScope).contains(especiallyForG1.getId()));
    assertFalse(idsOf(byScope).contains(forManager.getId()));
  }

  @Test
  void manager_read_by_id_ok() throws ApiException {
    var actual = apiAs(managerToken).getAnnouncementById(forAll.getId());

    assertEquals(forAll.getId(), actual.getId());
    assertEquals(forAll.getTitle(), actual.getTitle());
    assertEquals(GLOBAL, actual.getScope());
  }

  @Test
  void teacher_read_by_id_ok() throws ApiException {
    var actual = apiAs(teacherToken).getTeacherAnnouncementById(forTeacher.getId());
    assertEquals(forTeacher.getId(), actual.getId());
    assertEquals(TEACHER, actual.getScope());
  }

  @Test
  void student_read_by_id_ok() throws ApiException {
    var actual = apiAs(axelToken).getStudentsAnnouncementById(especiallyForG1.getId());
    assertEquals(especiallyForG1.getId(), actual.getId());
  }

  @Test
  void monitor_read_by_id_ok() throws ApiException {
    var actual = apiAs(monitorToken).getStudentsAnnouncementById(especiallyForG1.getId());
    assertEquals(especiallyForG1.getId(), actual.getId());
  }

  @Test
  void read_by_id_ko() {
    var apiStudent = apiAs(axelToken);
    var apiTeacher = apiAs(teacherToken);
    var apiMonitor = apiAs(monitorToken);

    assertThrowsForbiddenException(() -> apiStudent.getTeacherAnnouncementById(forTeacher.getId()));
    assertThrowsForbiddenException(() -> apiTeacher.getAnnouncementById(forManager.getId()));
    assertThrowsForbiddenException(() -> apiMonitor.getTeacherAnnouncementById(forTeacher.getId()));
  }

  @Test
  void manager_create_announcement_ok() throws ApiException {
    var toCreate =
        new CreateAnnouncement()
            .scope(STUDENT)
            .title("Cours de PROG1")
            .authorId(managerHasina.getId())
            .content("Cours prevu pour la semaine prochaine")
            .targetGroupList(
                List.of(new GroupIdentifier().id(groupG1.getId()).ref(groupG1.getRef())));

    var created = apiAs(managerToken).createAnnouncement(toCreate);

    assertEquals(toCreate.getTitle(), created.getTitle());
    assertEquals(toCreate.getScope(), created.getScope());
    assertEquals(managerHasina.getId(), created.getAuthor().getId());
    announcementRepository.deleteById(created.getId());
  }

  @Test
  void student_read_only_announcement_for_student_ok() throws ApiException {
    var actual = apiAs(axelToken).getStudentsAnnouncements(1, 100, null, null, null, null);

    assertTrue(idsOf(actual).contains(forAll.getId()));
    assertTrue(idsOf(actual).contains(especiallyForG1.getId()));
    assertFalse(idsOf(actual).contains(forTeacher.getId()));
  }

  @Test
  void monitor_read_only_announcement_for_student_ok() throws ApiException {
    var actual = apiAs(monitorToken).getStudentsAnnouncements(1, 100, null, null, null, null);

    assertTrue(idsOf(actual).contains(forAll.getId()));
    assertTrue(idsOf(actual).contains(especiallyForG1.getId()));
    assertFalse(idsOf(actual).contains(forTeacher.getId()));
  }

  @Test
  void student_read_all_announcement_or_for_teacher_ko() {
    var api = apiAs(axelToken);

    assertThrowsForbiddenException(() -> api.getAnnouncements(1, 15, null, null, null, null));
    assertThrowsForbiddenException(() -> api.getTeachersAnnouncements(1, 15, null, null, null));
  }

  @Test
  void monitor_read_all_announcement_or_for_teacher_ko() {
    var api = apiAs(monitorToken);

    assertThrowsForbiddenException(() -> api.getAnnouncements(1, 15, null, null, null, null));
    assertThrowsForbiddenException(() -> api.getTeachersAnnouncements(1, 15, null, null, null));
  }

  @Test
  void teacher_read_announcements_ok() throws ApiException {
    var actual = apiAs(teacherToken).getTeachersAnnouncements(1, 100, null, null, null);

    assertTrue(
        idsOf(actual)
            .containsAll(List.of(forAll.getId(), forTeacher.getId(), especiallyForG1.getId())));
    assertFalse(idsOf(actual).contains(forManager.getId()));
  }

  @Test
  void admin_react_announcement_ok() throws ApiException {
    var api = apiAs(adminToken);

    var before = api.getAnnouncementById(forManager.getId());

    var afterCheck =
        api.reactToAnnouncement(
            forManager.getId(), new ReactToAnnouncementRequest().reaction(CHECK));
    assertEquals(before.getReactionCount() + 1, afterCheck.getReactionCount());
    assertEquals(
        Boolean.TRUE, api.getAnnouncementById(forManager.getId()).getHasCurrentUserReaction());

    var afterUncheck =
        api.reactToAnnouncement(
            forManager.getId(), new ReactToAnnouncementRequest().reaction(UNCHECK));
    assertEquals(afterCheck.getReactionCount() - 1, afterUncheck.getReactionCount());
    assertNotEquals(
        Boolean.TRUE, api.getAnnouncementById(forManager.getId()).getHasCurrentUserReaction());
  }
}
