package school.hei.haapi.integration;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L1;
import static school.hei.haapi.integration.conf.ApiAssertions.assertBadRequestException;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestAuth.tokenFor;
import static school.hei.haapi.integration.conf.TestMocks.setUpS3Service;
import static school.hei.haapi.integration.testData.ManagerTestData.hasina;
import static school.hei.haapi.integration.testData.StudentTestData.axel;
import static school.hei.haapi.integration.testData.TeacherTestData.toky;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import school.hei.haapi.endpoint.rest.api.CoursesApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CourseDirection;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.UserRepository;

class CourseIT extends FacadeITMockedThirdParties {
  private String codePrefix;

  @Autowired private UserRepository userRepository;
  @Autowired private CourseRepository courseRepository;

  private User studentAxel;
  private User teacherToky;
  private User managerHasina;

  private Course lightCourse;
  private Course mediumCourse;
  private Course heavyCourse;

  private String studentToken;
  private String teacherToken;
  private String managerToken;

  private void setUpTestData() {
    studentAxel = userRepository.save(axel());
    teacherToky = userRepository.save(toky());
    managerHasina = userRepository.save(hasina());

    codePrefix = "CIT" + randomUUID().toString().substring(0, 8);
    lightCourse = courseRepository.save(aCourse(codePrefix + "A", "Algorithmique", 2));
    mediumCourse = courseRepository.save(aCourse(codePrefix + "B", "Bases de donnees", 6));
    heavyCourse = courseRepository.save(aCourse(codePrefix + "C", "Compilation", 10));
  }

  @BeforeEach
  void setUp() {
    setUpTestData();
    setUpS3Service(fileService, studentAxel);

    studentToken = tokenFor(casdoorAuthServiceMock, studentAxel);
    teacherToken = tokenFor(casdoorAuthServiceMock, teacherToky);
    managerToken = tokenFor(casdoorAuthServiceMock, managerHasina);
  }

  @AfterEach
  void tearDown() {
    courseRepository.deleteAll(
        courseRepository.findAll().stream()
            .filter(c -> c.getCode() != null && c.getCode().startsWith(codePrefix))
            .toList());
    userRepository.deleteAll(List.of(studentAxel, teacherToky, managerHasina));
  }

  private CoursesApi apiAs(String token) {
    return new CoursesApi(anApiClient(token));
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  private static Course aCourse(String code, String name, int credits) {
    return Course.builder()
        .id(randomUUID().toString())
        .code(code)
        .name(name)
        .credits(credits)
        .totalHours(80)
        .studentLevel(L1)
        .build();
  }

  private school.hei.haapi.endpoint.rest.model.Course aCreatableCourse() {
    return new school.hei.haapi.endpoint.rest.model.Course()
        .code(codePrefix + randomUUID().toString().substring(0, 4))
        .name("Nouveau cours")
        .credits(4)
        .totalHours(40)
        .level(L1);
  }

  private static List<String> idsOf(List<school.hei.haapi.endpoint.rest.model.Course> courses) {
    return courses.stream().map(course -> course.getId()).toList();
  }

  @Test
  void student_read_ok() throws ApiException {
    var api = apiAs(studentToken);

    var ownCourses = api.getCourses(codePrefix, null, null, null, null, null, null, 1, 100, null);
    var byId = api.getCourseById(lightCourse.getId());

    assertEquals(3, ownCourses.size());
    assertTrue(idsOf(ownCourses).contains(lightCourse.getId()));
    assertEquals(lightCourse.getId(), byId.getId());
    assertEquals(lightCourse.getCode(), byId.getCode());
  }

  @Test
  void teacher_read_ok() throws ApiException {
    var api = apiAs(teacherToken);

    var ownCourses = api.getCourses(codePrefix, null, null, null, null, null, null, 1, 100, null);
    var byId = api.getCourseById(mediumCourse.getId());

    assertTrue(idsOf(ownCourses).contains(mediumCourse.getId()));
    assertEquals(mediumCourse.getId(), byId.getId());
  }

  @Test
  void user_read_by_filter() throws ApiException {
    var api = apiAs(managerToken);

    var byExactCode =
        api.getCourses(lightCourse.getCode(), null, null, null, null, null, null, 1, 100, null);
    assertEquals(1, byExactCode.size());
    assertEquals(lightCourse.getId(), byExactCode.getFirst().getId());

    var byCodePrefix = api.getCourses(codePrefix, null, null, null, null, null, null, 1, 100, null);
    assertEquals(3, byCodePrefix.size());

    var byName = api.getCourses(codePrefix, "compil", null, null, null, null, null, 1, 100, null);
    assertEquals(1, byName.size());
    assertEquals(heavyCourse.getId(), byName.getFirst().getId());

    var byCredits = api.getCourses(codePrefix, null, 6, null, null, null, null, 1, 100, null);
    assertEquals(1, byCredits.size());
    assertEquals(mediumCourse.getId(), byCredits.getFirst().getId());

    var creditsAsc =
        api.getCourses(codePrefix, null, null, null, null, CourseDirection.ASC, null, 1, 100, null);
    assertEquals(
        List.of(lightCourse.getId(), mediumCourse.getId(), heavyCourse.getId()), idsOf(creditsAsc));

    var creditsDesc =
        api.getCourses(
            codePrefix, null, null, null, null, CourseDirection.DESC, null, 1, 100, null);
    assertEquals(
        List.of(heavyCourse.getId(), mediumCourse.getId(), lightCourse.getId()),
        idsOf(creditsDesc));
  }

  @Test
  void manager_create_or_update_ok() throws ApiException {
    var api = apiAs(managerToken);

    var toUpdate =
        new school.hei.haapi.endpoint.rest.model.Course()
            .id(lightCourse.getId())
            .code(lightCourse.getCode())
            .name("Algorithmique avancee")
            .credits(lightCourse.getCredits())
            .totalHours(lightCourse.getTotalHours())
            .level(L1);

    var updated = api.createOrUpdateCourses(List.of(toUpdate));
    assertEquals(1, updated.size());
    assertEquals("Algorithmique avancee", updated.getFirst().getName());

    var toAdd = List.of(aCreatableCourse(), aCreatableCourse());
    var added = api.createOrUpdateCourses(toAdd);
    assertEquals(2, added.size());

    var all = api.getCourses(codePrefix, null, null, null, null, null, null, 1, 100, null);
    assertEquals(5, all.size());
  }

  @Test
  void manager_create_or_update_bad_course_ko() {
    var api = apiAs(managerToken);

    assertBadRequestException(
        "code is mandatory",
        () -> api.createOrUpdateCourses(List.of(aCreatableCourse().code(null))));
    assertBadRequestException(
        "Name is mandatory",
        () -> api.createOrUpdateCourses(List.of(aCreatableCourse().name(null))));
    assertBadRequestException(
        "Credits must be positive",
        () -> api.createOrUpdateCourses(List.of(aCreatableCourse().credits(-1))));
    assertBadRequestException(
        "Total hours must be positive",
        () -> api.createOrUpdateCourses(List.of(aCreatableCourse().totalHours(-2))));
  }

  @Test
  void student_create_or_update_ko() {
    var api = apiAs(studentToken);

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.createOrUpdateCourses(List.of(aCreatableCourse())));
  }

  @Test
  void teacher_create_or_update_ko() {
    var api = apiAs(teacherToken);

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.createOrUpdateCourses(List.of(aCreatableCourse())));
  }

  @Test
  void manager_create_course_with_existing_code_ko() {
    var api = apiAs(managerToken);
    var duplicate = aCreatableCourse().code(heavyCourse.getCode());

    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Course."
            + heavyCourse.getCode()
            + " already exist.\"}",
        () -> api.createOrUpdateCourses(List.of(duplicate)));
  }
}
