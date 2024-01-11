package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.integration.conf.TestUtils.AWARDED_COURSE1_ID;
import static school.hei.haapi.integration.conf.TestUtils.COURSE1_ID;
import static school.hei.haapi.integration.conf.TestUtils.GROUP1_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_ID;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.awardedCourse1;
import static school.hei.haapi.integration.conf.TestUtils.awardedCourse2;
import static school.hei.haapi.integration.conf.TestUtils.awardedCourse3;
import static school.hei.haapi.integration.conf.TestUtils.awardedCourse4;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.conf.FacadeIT;
import school.hei.haapi.endpoint.rest.api.TeachingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.AwardedCourse;
import school.hei.haapi.endpoint.rest.model.CreateAwardedCourse;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.TestUtils;

@Testcontainers
class AwardedCourseIT extends FacadeIT {
  @LocalServerPort private int serverPort;
  @MockBean private SentryConf sentryConf;
  @MockBean private CognitoComponent cognitoComponentMock;

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, serverPort);
  }

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
  }

  @Test
  void manager_read_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(manager1Client);

    AwardedCourse actual = api.getAwardedCoursesByIdAndGroupId(GROUP1_ID, AWARDED_COURSE1_ID);

    List<AwardedCourse> actuals = api.getAllAwardedCourseByGroup(GROUP1_ID, 1, 10);

    List<AwardedCourse> allAwardedCourse =
        api.getAllAwardedCourseByCriteria(null, null, null, null);

    List<AwardedCourse> awardedCoursesByTeacher =
        api.getAllAwardedCourseByCriteria(TEACHER1_ID, null, null, null);

    List<AwardedCourse> awardedCoursesByCourse =
        api.getAllAwardedCourseByCriteria(null, COURSE1_ID, null, null);

    assertEquals(awardedCourse1(), actual);

    assertEquals(3, actuals.size());
    assertTrue(actuals.contains(awardedCourse1()));
    assertTrue(actuals.contains(awardedCourse2()));
    assertTrue(actuals.contains(awardedCourse4()));

    assertEquals(5, allAwardedCourse.size());
    assertTrue(allAwardedCourse.contains(awardedCourse1()));
    assertTrue(allAwardedCourse.contains(awardedCourse2()));
    assertTrue(allAwardedCourse.contains(awardedCourse3()));
    assertTrue(allAwardedCourse.contains(awardedCourse4()));

    assertEquals(1, awardedCoursesByTeacher.size());
    assertTrue(awardedCoursesByTeacher.contains(awardedCourse1()));

    assertEquals(3, awardedCoursesByCourse.size());
    assertTrue(awardedCoursesByCourse.contains(awardedCourse1()));
    assertTrue(awardedCoursesByCourse.contains(awardedCourse2()));
    assertTrue(awardedCoursesByCourse.contains(awardedCourse3()));
  }

  @Test
  void student_read_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    TeachingApi api = new TeachingApi(student1Client);
    assertThrowsForbiddenException(
        () -> api.getAwardedCoursesByIdAndGroupId(GROUP1_ID, AWARDED_COURSE1_ID));
    assertThrowsForbiddenException(() -> api.getAllAwardedCourseByGroup(GROUP1_ID, 1, 10));
  }

  @Test
  void teacher_read_ok() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    TeachingApi api = new TeachingApi(teacher1Client);
    AwardedCourse actual = api.getAwardedCoursesByIdAndGroupId(GROUP1_ID, AWARDED_COURSE1_ID);

    List<AwardedCourse> actuals = api.getAllAwardedCourseByGroup(GROUP1_ID, 1, 10);

    List<AwardedCourse> allAwardedCourse =
        api.getAllAwardedCourseByCriteria(null, null, null, null);

    List<AwardedCourse> awardedCoursesByTeacher =
        api.getAllAwardedCourseByCriteria(TEACHER1_ID, null, null, null);

    List<AwardedCourse> awardedCoursesByCourse =
        api.getAllAwardedCourseByCriteria(null, COURSE1_ID, null, null);

    assertEquals(awardedCourse1(), actual);

    assertEquals(3, actuals.size());
    assertTrue(actuals.contains(awardedCourse1()));
    assertTrue(actuals.contains(awardedCourse2()));
    assertTrue(actuals.contains(awardedCourse4()));

    assertEquals(5, allAwardedCourse.size());
    assertTrue(allAwardedCourse.contains(awardedCourse1()));
    assertTrue(allAwardedCourse.contains(awardedCourse2()));
    assertTrue(allAwardedCourse.contains(awardedCourse3()));
    assertTrue(allAwardedCourse.contains(awardedCourse4()));

    assertEquals(1, awardedCoursesByTeacher.size());
    assertTrue(awardedCoursesByTeacher.contains(awardedCourse1()));

    assertEquals(3, awardedCoursesByCourse.size());
    assertTrue(awardedCoursesByCourse.contains(awardedCourse1()));
    assertTrue(awardedCoursesByCourse.contains(awardedCourse2()));
    assertTrue(awardedCoursesByCourse.contains(awardedCourse3()));
  }

  @Test
  void student_create_or_update_ko() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    TeachingApi api = new TeachingApi(student1Client);
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.createOrUpdateAwardedCourses(GROUP1_ID, List.of(createAwardedCourse())));
  }

  @Test
  void teacher_create_or_update_ko() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    TeachingApi api = new TeachingApi(teacher1Client);
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.createOrUpdateAwardedCourses(GROUP1_ID, List.of(createAwardedCourse())));
  }

  @Test
  void manager_create_or_update_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(manager1Client);

    List<AwardedCourse> actualCreatedAwardedCourses =
        api.createOrUpdateAwardedCourses(GROUP1_ID, List.of(createAwardedCourse()));

    assertEquals(1, actualCreatedAwardedCourses.size());
  }

  private static CreateAwardedCourse createAwardedCourse() {
    return new CreateAwardedCourse()
        .courseId("course2_id")
        .groupId("group2_id")
        .mainTeacherId("teacher2_id");
  }
}
