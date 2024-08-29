package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.MONITOR1_ID;
import static school.hei.haapi.integration.conf.TestUtils.MONITOR1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.coordinatesWithValues;
import static school.hei.haapi.integration.conf.TestUtils.monitor1;
import static school.hei.haapi.endpoint.rest.model.EnableStatus.ENABLED;
import static school.hei.haapi.endpoint.rest.model.ProfessionalExperienceFileTypeEnum.WORKER_STUDENT;
import static school.hei.haapi.endpoint.rest.model.Sex.F;
import static school.hei.haapi.endpoint.rest.model.Sex.M;
import static school.hei.haapi.endpoint.rest.model.SpecializationField.COMMON_CORE;
import static school.hei.haapi.endpoint.rest.model.WorkStudyStatus.WORKING;
import static school.hei.haapi.integration.conf.TestUtils.coordinatesWithNullValues;
import static school.hei.haapi.integration.conf.TestUtils.group1;
import static school.hei.haapi.integration.conf.TestUtils.group2;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpEventBridge;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.UsersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CrupdateMonitor;
import school.hei.haapi.endpoint.rest.model.EnableStatus;
import school.hei.haapi.endpoint.rest.model.Monitor;
import school.hei.haapi.endpoint.rest.model.Sex;
import school.hei.haapi.endpoint.rest.model.Coordinates;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.endpoint.rest.model.UserIdentifier;
import school.hei.haapi.integration.conf.MockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = MonitorIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class MonitorIT extends MockedThirdParties {
  @MockBean private EventBridgeClient eventBridgeClientMock;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, MonitorIT.ContextInitializer.SERVER_PORT);

  public static Student student1() {
    Student student = new Student();
    student.setId("student1_id");
    student.setFirstName("Ryan");
    student.setLastName("Andria");
    student.setEmail("test+ryan@hei.school");
    student.setRef("STD21001");
    student.setPhone("0322411123");
    student.setStatus(ENABLED);
    student.setSex(M);
    student.setBirthDate(LocalDate.parse("2000-01-01"));
    student.setEntranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"));
    student.setAddress("Adr 1");
    student.setNic("");
    student.setSpecializationField(COMMON_CORE);
    student.setBirthPlace("");
    student.setCoordinates(new Coordinates().longitude(-123.123).latitude(123.0));
    student.setHighSchoolOrigin("Lycée Andohalo");
    student.setWorkStudyStatus(WORKING);
    student.setProfessionalExperience(WORKER_STUDENT);
    student.setCommitmentBeginDate(Instant.parse("2021-11-08T08:25:24Z"));
    student.setGroups(List.of(group1(), group2()));
    student.setIsRepeatingYear(false);
    return student;
  }

  public static Student student2() {
    Student student = new Student();
    student.setId("student2_id");
    student.setFirstName("Two");
    student.setLastName("Student");
    student.setEmail("test+student2@hei.school");
    student.setRef("STD21002");
    student.setPhone("0322411124");
    student.setStatus(ENABLED);
    student.setSex(F);
    student.setBirthDate(LocalDate.parse("2000-01-02"));
    student.setEntranceDatetime(Instant.parse("2021-11-09T08:26:24.00Z"));
    student.setAddress("Adr 2");
    student.setBirthPlace("");
    student.setNic("");
    student.setSpecializationField(COMMON_CORE);
    student.setCoordinates(new Coordinates().longitude(255.255).latitude(-255.255));
    student.setHighSchoolOrigin("Lycée Andohalo");
    student.setWorkStudyStatus(WORKING);
    student.setProfessionalExperience(WORKER_STUDENT);
    student.setCommitmentBeginDate(Instant.parse("2021-11-08T08:25:24.00Z"));
    student.setGroups(List.of(group1()));
    student.setIsRepeatingYear(false);
    return student;
  }

  public static Monitor monitor1() {
    Monitor monitor = new Monitor();
    monitor.setId("monitor1_id");
    monitor.setFirstName("Monitor");
    monitor.setLastName("One");
    monitor.setEmail("test+monitor@hei.school");
    monitor.setRef("MTR21001");
    monitor.setPhone("0322411123");
    monitor.setStatus(ENABLED);
    monitor.setSex(F);
    monitor.setBirthDate(LocalDate.parse("2000-01-01"));
    monitor.setEntranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"));
    monitor.setAddress("Adr 6");
    monitor.setNic("");
    monitor.setBirthPlace("");
    monitor.coordinates(coordinatesWithNullValues());
    monitor.setHighSchoolOrigin(null);
    monitor.setStudents(List.of(student1(), student2()));
    return monitor;
  }

  public static UserIdentifier identifierStudent1() {
    UserIdentifier userIdentifier = new UserIdentifier();
    userIdentifier.setId("student1_id");
    userIdentifier.setFirstName("Ryan");
    userIdentifier.setLastName("Andria");
    userIdentifier.setEmail("test+ryan@hei.school");
    userIdentifier.setRef("STD21001");
    userIdentifier.setNic("");
    return userIdentifier;
  }

  public static UserIdentifier identifierStudent2() {
    UserIdentifier userIdentifier = new UserIdentifier();
    userIdentifier.setId("student2_id");
    userIdentifier.setFirstName("Two");
    userIdentifier.setLastName("Student");
    userIdentifier.setEmail("test+student2@hei.school");
    userIdentifier.setRef("STD21002");
    userIdentifier.setNic("");
    return userIdentifier;
  }

  public static CrupdateMonitor someCreatableMonitor1() {
    CrupdateMonitor monitor = new CrupdateMonitor();
    monitor.setId("monitor1_id");
    monitor.setFirstName("Monitor");
    monitor.setLastName("One");
    monitor.setEmail("test+monitor@hei.school");
    monitor.setRef("MTR21001");
    monitor.setPhone("0322411123");
    monitor.setStatus(ENABLED);
    monitor.setSex(F);
    monitor.setBirthDate(LocalDate.parse("2000-01-01"));
    monitor.setEntranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"));
    monitor.setAddress("Adr 6");
    monitor.setNic("");
    monitor.setBirthPlace("");
    monitor.coordinates(coordinatesWithNullValues());
    monitor.setHighSchoolOrigin(null);
    return monitor;
  }

  @BeforeEach
  public void setUp() {
    setUpCognito(cognitoComponentMock);
    setUpEventBridge(eventBridgeClientMock);
  }

  @Test
  void monitor_read_own_ok() throws ApiException {
    ApiClient monitor1Client = anApiClient(MONITOR1_TOKEN);

    UsersApi api = new UsersApi(monitor1Client);
    Monitor actual = api.getMonitorById(MONITOR1_ID);

    assertEquals(monitor1(), actual);
  }

  @Test
  void manager_read_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);

    UsersApi api = new UsersApi(manager1Client);
    Monitor actual = api.getMonitorById(MONITOR1_ID);

    assertEquals(monitor1(), actual);
  }

  @Test
  void monitor_read_other_ko() {
    ApiClient monitor1Client = anApiClient(MONITOR1_TOKEN);

    UsersApi api = new UsersApi(monitor1Client);
    assertThrowsForbiddenException(() -> api.getMonitorById("monitor2_id"));
  }

  @Test
  void student_read_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

    UsersApi api = new UsersApi(student1Client);
    assertThrowsForbiddenException(() -> api.getMonitorById(MONITOR1_ID));
  }

  @Test
  void teacher_read_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);

    UsersApi api = new UsersApi(teacher1Client);
    assertThrowsForbiddenException(() -> api.getMonitorById(MONITOR1_ID));
  }

  @Test
  @DirtiesContext
  void manager_update_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);

    UsersApi api = new UsersApi(manager1Client);
    Monitor actual = api.updateMonitorById(MONITOR1_ID, someUpdatableMonitor1());
    Monitor expected = api.getMonitorById(MONITOR1_ID);

    assertEquals(actual, expected);
  }

  public static CrupdateMonitor someUpdatableMonitor1() {
      return new CrupdateMonitor()
              .id("monitor1_id")
              .email("test+monitor@hei.school")
              .ref("MTR21001")
              .phone("0322411123")
              .status(EnableStatus.ENABLED)
              .sex(Sex.F)
              .entranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
              .nic("")
              .birthPlace("")
              .address("Adr 111")
              .sex(Sex.M)
              .lastName("Other lastname")
              .firstName("Other firstname")
              .birthDate(LocalDate.parse("2000-01-01"))
              .coordinates(coordinatesWithValues());
    }

  void manager_write_monitor_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Monitor> monitors = api.createOrUpdateMonitors(List.of(someCreatableMonitor1()));
    Monitor monitor1 = monitors.get(0);

    assertEquals(1, monitors.size());
    assertEquals(monitor1(), monitor1);
    assertEquals(2, monitor1.getStudents().size());
    assertEquals(student1(), monitor1.getStudents().get(0));
    assertEquals(student2(), monitor1.getStudents().get(1));
  }
}
