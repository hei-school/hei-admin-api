package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.TestUtils.COURSE1_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.course1;
import static school.hei.haapi.integration.conf.TestUtils.course2;
import static school.hei.haapi.integration.conf.TestUtils.course3;
import static school.hei.haapi.integration.conf.TestUtils.createCourse;
import static school.hei.haapi.integration.conf.TestUtils.isBefore;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;
import static school.hei.haapi.integration.conf.TestUtils.someCreatableCourseList;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.TeachingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.CourseDirection;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.file.BucketConf;
import school.hei.haapi.file.S3Conf;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.service.aws.FileService;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = CourseIT.ContextInitializer.class)
@AutoConfigureMockMvc
class CourseIT {
  @MockBean private SentryConf sentryConf;
  @MockBean private CognitoComponent cognitoComponentMock;
  @MockBean BucketConf bucketConf;
  @MockBean
  S3Conf s3Conf;
  @MockBean
  FileService fileService;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, CourseIT.ContextInitializer.SERVER_PORT);
  }

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
    setUpS3Service(fileService, student1());
  }

  @Test
  void student_read_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    TeachingApi api = new TeachingApi(student1Client);

    List<Course> actualList = api.getCourses(null, null, null, null, null, null, null, null, null);
    Course actual = api.getCourseById(COURSE1_ID);

    assertEquals(4, actualList.size());
    assertTrue(actualList.contains(course1()));

    assertEquals(course1(), actual);
  }

  @Test
  void teacher_read_ok() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    TeachingApi api = new TeachingApi(teacher1Client);

    List<Course> actualList = api.getCourses(null, null, null, null, null, null, null, null, null);
    assertEquals(4, actualList.size());
    assertTrue(actualList.contains(course2()));

    Course actual = api.getCourseById(COURSE1_ID);
    assertEquals(course1(), actual);
  }

  @Test
  void user_read_by_filter() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(manager1Client);

    List<Course> actualByCode =
        api.getCourses("PROG1", null, null, null, null, null, null, null, null);

    List<Course> actualByCode2 =
        api.getCourses("PROG", null, null, null, null, null, null, null, null);

    List<Course> actualByCredits2 =
        api.getCourses(null, null, 6, null, null, null, null, null, null);

    List<Course> actualByLastName =
        api.getCourses(null, null, null, null, "tEaC", null, null, null, null);

    List<Course> actualByCodeAndName =
        api.getCourses("i", "i", null, null, null, null, null, null, null);

    List<Course> actualByCreditsOrderedAsc =
        api.getCourses(null, null, null, null, null, CourseDirection.ASC, null, null, null);

    List<Course> actualByCreditsOrderedDesc =
        api.getCourses(null, null, null, null, null, CourseDirection.DESC, null, null, null);

    assertEquals(1, actualByCode.size());
    assertTrue(actualByCode.contains(course1()));

    assertEquals(2, actualByCode2.size());
    assertTrue(actualByCode2.contains(course1()));
    assertTrue(actualByCode2.contains(course2()));

    assertEquals(2, actualByCredits2.size());
    assertTrue(actualByCredits2.contains(course1()));
    assertTrue(actualByCredits2.contains(course2()));

    assertEquals(2, actualByLastName.size());
    assertTrue(actualByLastName.contains(course1()));

    assertEquals(1, actualByCodeAndName.size());
    assertTrue(actualByCodeAndName.contains(course3()));

    assertEquals(4, actualByCreditsOrderedAsc.size());
    assertTrue(
        isBefore(
            actualByCreditsOrderedAsc.get(0).getCredits(),
            actualByCreditsOrderedAsc.get(2).getCredits()));

    assertEquals(4, actualByCreditsOrderedDesc.size());
    assertTrue(
        isBefore(
            actualByCreditsOrderedDesc.get(3).getCredits(),
            actualByCreditsOrderedDesc.get(1).getCredits()));
  }

  @Test
  void manager_create_or_update_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(manager1Client);

    List<Course> actualUpdate = api.createOrUpdateCourses(List.of(course2(), course1()));

    assertEquals(2, actualUpdate.size());
    assertTrue(actualUpdate.contains(course2()));
    assertTrue(actualUpdate.contains(course1()));

    int numberOfCourseToAdd = 2;

    List<Course> coursesToAdd = someCreatableCourseList(numberOfCourseToAdd);
    List<Course> actualAdd = api.createOrUpdateCourses(coursesToAdd);
    assertEquals(numberOfCourseToAdd, actualAdd.size());
    assertTrue(actualAdd.contains(coursesToAdd.get(0).id(actualAdd.get(0).getId())));

    List<Course> actualCourseList =
        api.getCourses(null, null, null, null, null, CourseDirection.DESC, null, null, null);

    assertEquals(4 + numberOfCourseToAdd, actualCourseList.size());
  }

  @Test
  void student_create_or_update_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    TeachingApi api = new TeachingApi(student1Client);
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.createOrUpdateCourses(someCreatableCourseList(1)));
  }

  @Test
  void Teacher_create_or_update_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    TeachingApi api = new TeachingApi(teacher1Client);
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.createOrUpdateCourses(someCreatableCourseList(1)));
  }

  @Test
  void manager_create_or_update_ko() {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(manager1Client);

    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Course.PROG3 already exist.\"}",
        () -> api.createOrUpdateCourses(List.of(createCourse("PROG3"))));
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
