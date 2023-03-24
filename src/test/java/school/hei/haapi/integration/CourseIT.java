package school.hei.haapi.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.TeachingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.Order;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.TeacherIT.teacher1;
import static school.hei.haapi.integration.TeacherIT.teacher2;
import static school.hei.haapi.integration.conf.TestUtils.COURSE1_ID;
import static school.hei.haapi.integration.conf.TestUtils.COURSE2_ID;
import static school.hei.haapi.integration.conf.TestUtils.COURSE3_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = CourseIT.ContextInitializer.class)
@AutoConfigureMockMvc
class CourseIT {
  @MockBean
  private SentryConf sentryConf;
  @MockBean
  private CognitoComponent cognitoComponentMock;

  @Autowired
  private static UserService userService;

  @Autowired
  private static UserMapper userMapper;

  private static ApiClient anApiClient(String token){
    return TestUtils.anApiClient(token, ContextInitializer.SERVER_PORT);
  }

  static Course course1(){
    Course course = new Course();
    course.setId(COURSE1_ID);
    course.setCode("PROG1");
    course.setName("Algorithmics");
    course.setCredits(6);
    course.setTotalHours(0);
    course.setMainTeacher(teacher1());
    return course;
  }
  static Course course2(){
    Course course = new Course();
    course.setId(COURSE2_ID);
    course.setCode("PROG3");
    course.setName("P.O.O avancée");
    course.setCredits(6);
    course.setTotalHours(0);
    course.setMainTeacher(teacher2());
    return course;
  }
  static Course course3(){
    Course course = new Course();
    course.setId(COURSE3_ID);
    course.setCode("WEB1");
    course.setName("Interface web");
    course.setCredits(4);
    course.setTotalHours(0);
    course.setMainTeacher(teacher2());
    return course;
  }

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
  }

  @Test
  void manager_read_filtered_by_firstname_ok() throws ApiException {
    ApiClient managerApiClient = anApiClient(MANAGER1_TOKEN);
    TeachingApi teachingApi = new TeachingApi(managerApiClient);

    List<Course> courseList = teachingApi.getCourses(1, 5, null, null, null, "One", null, null, null);
    Course actualCourse = courseList.get(0);
    List<Course> courseList2 = teachingApi.getCourses(1,5, null, null, null,"tW",null, null, null);

    assertEquals(course1(), actualCourse);
    assertTrue(courseList2.contains(course2()));
    assertTrue(courseList2.contains(course3()));
  }

  @Test
  void manager_read_filtered_by_code_or_by_credits_or_by_name_ok() throws ApiException{
    ApiClient managerApiClient = anApiClient(MANAGER1_TOKEN);
    TeachingApi teachingApi = new TeachingApi(managerApiClient);

    List<Course> courseList = teachingApi.getCourses(1,5,"PROG1", null, null, null, null, null, null);
    Course actualCourse = courseList.get(0);
    List<Course> courseList2 = teachingApi.getCourses(1,5,null,null,6,null,null, null, null);
    List<Course> courseList3 = teachingApi.getCourses(1,5,null, "Interface web", null,null,null, null, null);

    assertEquals(course1(), actualCourse);
    assertTrue(courseList2.contains(course2()));
    assertTrue(courseList3.contains(course3()));

  }

  @Test
  void manager_read_sorted_and_filtered_by_all_criteria_ok() throws ApiException{
    ApiClient managerApiClient = anApiClient(MANAGER1_TOKEN);
    TeachingApi teachingApi = new TeachingApi(managerApiClient);

    List<Course> actualCourse = teachingApi.getCourses(1,5, "PROG1", "Algorithmique", 6, "One", "Teacher", Order.ASC, Order.ASC);

    assertEquals(course1(), actualCourse.get(0));
  }

  @Test
  void manager_read_ok() throws ApiException{
    ApiClient managerApiClient = anApiClient(MANAGER1_TOKEN);
    TeachingApi teachingApi = new TeachingApi(managerApiClient);

    List<Course> courseList = teachingApi.getCourses(1,5,null,null,null,null, null, null, null);

    assertTrue(courseList.contains(course1()));
  }

  @Test
  void manager_read_sorted_by_credits_ok() throws ApiException{
    ApiClient managerApiClient = anApiClient(MANAGER1_TOKEN);
    TeachingApi teachingApi = new TeachingApi(managerApiClient);

    List<Course> courseList = teachingApi.getCourses(1, 5,null,null,null,null,null, Order.ASC, null);

    assertTrue(courseList.contains(course1()));
    assertEquals(course1(), courseList.get(0));
  }

  @Test
  void manager_read_sorted_by_code_ok() throws  ApiException{
    ApiClient managerApiClient = anApiClient(MANAGER1_TOKEN);
    TeachingApi teachingApi = new TeachingApi(managerApiClient);

    List<Course> courseList = teachingApi.getCourses(1, 5,null,null,null,null,null, null,Order.ASC);

    assertTrue(courseList.contains(course1()));
    assertEquals(course1(), courseList.get(0));
  }

  @Test
  void manager_read_sorted_by_credits_and_code_ok() throws ApiException{
    ApiClient managerApiClient = anApiClient(MANAGER1_TOKEN);
    TeachingApi teachingApi = new TeachingApi(managerApiClient);

    List<Course> courseList = teachingApi.getCourses(1, 5,null,null,null,null,null, Order.ASC,Order.DESC);

    assertTrue(courseList.contains(course1()));
    assertEquals(course1(), courseList.get(0));
  }
  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}