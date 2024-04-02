package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.TestUtils.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
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
import school.hei.haapi.endpoint.rest.model.CrupdateWorkStudyStudent;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.endpoint.rest.model.WorkStudyInfo;
import school.hei.haapi.endpoint.rest.model.WorkStudyStudent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.MockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = WorkInfoIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class WorkInfoIT extends MockedThirdParties {
  @MockBean private EventBridgeClient eventBridgeClientMock;
  @Autowired ObjectMapper objectMapper;

  public static String STUDENT1_REF = "STD21001";

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, ContextInitializer.SERVER_PORT);
  }

  @BeforeEach
  public void setUp() {
    setUpCognito(cognitoComponentMock);
    setUpEventBridge(eventBridgeClientMock);
    setUpS3Service(fileService, student1());
  }

  @Test
  void student_read_other_work_info_ko() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    UsersApi api = new UsersApi(student1Client);

    assertThrowsForbiddenException(() -> api.getStudentWorkInfo(STUDENT2_ID));
  }

  @Test
  void manager_read_student_work_info_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<WorkStudyStudent> actual = api.getStudentWorkInfo(STUDENT1_ID);

    assertEquals(1, actual.size());
    assertEquals(workStudyStudent1(), actual.get(0));
  }

  @Test
  @DirtiesContext
  void manager_create_student_work_info_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<WorkStudyStudent> actual0 =
        api.createOrUpdateStudentWorkStudyInfo(STUDENT1_ID, List.of(creatableWorkInfo1()));

    assertEquals(1, actual0.size());

    List<WorkStudyStudent> expected =
        List.of(workStudyStudent1().workStudyInfo(List.of(workInfo1(), createdWorkInfo())));

    List<WorkStudyStudent> actual1 = api.getStudentWorkInfo(STUDENT1_ID);

    assertEquals(
        expected.get(0).getWorkStudyInfo().get(1).getBusiness(),
        actual1.get(0).getWorkStudyInfo().get(1).getBusiness());

    List<Student> actualStudents =
        api.getStudents(1, 10, STUDENT1_REF, null, null, null, null, null, null);

    assertEquals(true, actualStudents.get(0).getIsWorkingStudy());
  }

  public static WorkStudyInfo createdWorkInfo() {
    return new WorkStudyInfo()
        .business("business 2")
        .company("company")
        .commitmentBeginDate(Instant.parse("2024-10-28T12:03:56.651Z"));
  }

  public static CrupdateWorkStudyStudent creatableWorkInfo1() {
    return new CrupdateWorkStudyStudent()
        .id("work_info2_id")
        .business("business 2")
        .company("company")
        .studentId(STUDENT1_ID);
  }

  public static WorkStudyInfo workInfo1() {
    return new WorkStudyInfo()
        .id("work_info1_id")
        .commitmentBeginDate(Instant.parse("2024-03-28T12:03:56.651Z"))
        .company("company")
        .business("Web Dev")
        .commitmentEndDate(Instant.parse("2024-09-28T12:03:56.651Z"));
  }

  public static WorkStudyStudent workStudyStudent1() {
    Student student1 = student1();

    return new WorkStudyStudent()
        .id(student1.getId())
        .ref(student1.getRef())
        .nic(student1.getNic())
        .firstName(student1.getFirstName())
        .lastName(student1.getLastName())
        .workStudyInfo(List.of(workInfo1()))
        .email(student1.getEmail());
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
