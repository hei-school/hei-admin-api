package school.hei.haapi.integration.conf;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.function.Executable;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.CreateFee;
import school.hei.haapi.endpoint.rest.model.CrupdateCourse;
import school.hei.haapi.endpoint.rest.model.EnableStatus;
import school.hei.haapi.endpoint.rest.model.Fee;
import school.hei.haapi.endpoint.rest.model.Teacher;
import school.hei.haapi.endpoint.rest.model.UpdateStudentCourse;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.CourseStatus.LINKED;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.LATE;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.PAID;
import static school.hei.haapi.endpoint.rest.model.Fee.TypeEnum.HARDWARE;
import static school.hei.haapi.endpoint.rest.model.Fee.TypeEnum.TUITION;

public class TestUtils {

  public static final String STUDENT1_ID = "student1_id";
  public static final String STUDENT2_ID = "student2_id";

  public static final String NON_EXISTENT_STUDENT_ID = "student99_id";
  public static final String TEACHER1_ID = "teacher1_id";
  public static final String TEACHER2_ID = "teacher2_id";
  public static final String TEACHER3_ID = "teacher3_id";
  public static final String TEACHER4_ID = "teacher4_id";
  public static final String MANAGER_ID = "manager1_id";
  public static final String GROUP1_ID = "group1_id";
  public static final String FEE1_ID = "fee1_id";
  public static final String FEE2_ID = "fee2_id";
  public static final String FEE3_ID = "fee3_id";
  public static final String FEE4_ID = "fee4_id";
  public static final String FEE6_ID = "fee6_id";
  public static final String PAYMENT1_ID = "payment1_id";
  public static final String PAYMENT2_ID = "payment2_id";
  public static final String PAYMENT4_ID = "payment4_id";
  public static final String COURSE1_ID = "course1_id";
  public static final String COURSE2_ID = "course2_id";
  public static final String COURSE3_ID = "course3_id";
  public static final String COURSE4_ID = "course4_id";
  public static final String COURSE5_ID = "course5_id";
  public static final String TRANSCRIPT1_ID = "transcript1_id";
  public static final String TRANSCRIPT2_ID = "transcript2_id";
  public static final String TRANSCRIPT3_ID = "transcript3_id";
  public static final String TRANSCRIPT4_ID = "transcript4_id";
  public static final String NEW_TRANSCRIPT_ID = "new_transcript_id";

  public static final String TRANSCRIPT1_VERSION1_ID = "transcript1_version1_id";
  public static final String TRANSCRIPT1_VERSION2_ID = "transcript1_version2_id";
  public static final String TRANSCRIPT2_VERSION1_ID = "transcript2_version1_id";
  public static final String TRANSCRIPT2_VERSION2_ID = "transcript2_version2_id";
  public static final String TRANSCRIPT2_VERSION3_ID = "transcript2_version3_id";
  public static final String TRANSCRIPT3_VERSION1_ID = "transcript3_version1_id";
  public static final String TRANSCRIPT1_VERSION1_CLAIM1_ID = "transcript1_version1_claim1_id";
  public static final String TRANSCRIPT1_VERSION1_CLAIM2_ID = "transcript1_version1_claim2_id";

  public static final String TRANSCRIPT2_VERSION1_CLAIM1_ID = "transcript2_version1_claim1_id";
  public static final String TRANSCRIPT2_VERSION1_CLAIM2_ID = "transcript2_version1_claim2_id";
  public static final String TRANSCRIPT3_VERSION1_CLAIM1_ID = "transcript3_version1_claim1_id";

  public static final String BAD_TOKEN = "bad_token";
  public static final String STUDENT1_TOKEN = "student1_token";
  public static final String TEACHER1_TOKEN = "teacher1_token";
  public static final String MANAGER1_TOKEN = "manager1_token";

  public static ApiClient anApiClient(String token, int serverPort) {
    ApiClient client = new ApiClient();
    client.setScheme("http");
    client.setHost("localhost");
    client.setPort(serverPort);
    client.setRequestInterceptor(httpRequestBuilder ->
        httpRequestBuilder.header("Authorization", "Bearer " + token));
    return client;
  }

  public static void setUpCognito(CognitoComponent cognitoComponent) {
    when(cognitoComponent.getEmailByIdToken(BAD_TOKEN)).thenReturn(null);
    when(cognitoComponent.getEmailByIdToken(STUDENT1_TOKEN)).thenReturn("test+ryan@hei.school");
    when(cognitoComponent.getEmailByIdToken(TEACHER1_TOKEN)).thenReturn("test+teacher1@hei.school");
    when(cognitoComponent.getEmailByIdToken(MANAGER1_TOKEN)).thenReturn("test+manager1@hei.school");
  }

  public static void setUpEventBridge(EventBridgeClient eventBridgeClient) {
    when(eventBridgeClient.putEvents((PutEventsRequest) any())).thenReturn(
        PutEventsResponse.builder().build()
    );
  }

  public static void assertThrowsApiException(String expectedBody, Executable executable) {
    ApiException apiException = assertThrows(ApiException.class, executable);
    assertEquals(expectedBody, apiException.getResponseBody());
  }

  public static void assertThrowsForbiddenException(Executable executable) {
    ApiException apiException = assertThrows(ApiException.class, executable);
    String responseBody = apiException.getResponseBody();
    assertEquals("{"
        + "\"type\":\"403 FORBIDDEN\","
        + "\"message\":\"Access is denied\"}", responseBody);
  }

  public static Teacher teacher1() {
    return new Teacher()
        .id("teacher1_id")
        .firstName("One")
        .lastName("Teacher")
        .email("test+teacher1@hei.school")
        .ref("TCR21001")
        .phone("0322411125")
        .status(EnableStatus.ENABLED)
        .sex(Teacher.SexEnum.F)
        .birthDate(LocalDate.parse("1990-01-01"))
        .entranceDatetime(Instant.parse("2021-10-08T08:27:24.00Z"))
        .address("Adr 3");
  }

  public static Teacher teacher2() {
    return new Teacher()
        .id("teacher2_id")
        .firstName("Two")
        .lastName("Teacher")
        .email("test+teacher2@hei.school")
        .ref("TCR21002")
        .phone("0322411126")
        .status(EnableStatus.ENABLED)
        .sex(Teacher.SexEnum.M)
        .birthDate(LocalDate.parse("1990-01-02"))
        .entranceDatetime(Instant.parse("2021-10-09T08:28:24Z"))
        .address("Adr 4");
  }

  public static Teacher teacher3() {
    return new Teacher()
        .id("teacher3_id")
        .firstName("Three")
        .lastName("Teach")
        .email("test+teacher3@hei.school")
        .ref("TCR21003")
        .phone("0322411126")
        .status(EnableStatus.ENABLED)
        .sex(Teacher.SexEnum.M)
        .birthDate(LocalDate.parse("1990-01-02"))
        .entranceDatetime(Instant.parse("2021-10-09T08:28:24Z"))
        .address("Adr 4");
  }

  public static Teacher someCreatableTeacher() {
    return new Teacher()
        .firstName("Some")
        .lastName("User")
        .email(randomUUID() + "@hei.school")
        .ref("TCR21-" + randomUUID())
        .phone("0332511129")
        .status(EnableStatus.ENABLED)
        .sex(Teacher.SexEnum.M)
        .birthDate(LocalDate.parse("2000-01-01"))
        .entranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
        .address("Adr X");
  }

  public static CrupdateCourse crupdatedCourse1() {
    return new CrupdateCourse()
        .id(COURSE5_ID)
        .code("MGT1")
        .name("Collaborative work")
        .credits(5)
        .totalHours(12)
        .mainTeacherId(TEACHER4_ID);
  }

  public static CrupdateCourse crupdatedCourse2() {
    return new CrupdateCourse()
        .code("MGT1")
        .name("Collaborative work like GWSP")
        .credits(12)
        .totalHours(5)
        .mainTeacherId(TEACHER4_ID);
  }

  public static UpdateStudentCourse updateStudentCourse() {
    return new UpdateStudentCourse()
        .courseId(COURSE3_ID)
        .status(LINKED);
  }

  public static List<Teacher> someCreatableTeacherList(int nbOfTeacher) {
    List<Teacher> teacherList = new ArrayList<>();
    for (int i = 0; i < nbOfTeacher; i++) {
      teacherList.add(someCreatableTeacher());
    }
    return teacherList;
  }


  public static Teacher teacher4() {
    return new Teacher()
        .id(TEACHER4_ID)
        .firstName("Four")
        .lastName("Binary")
        .email("test+teacher4@hei.school")
        .ref("TCR21004")
        .phone("0322411426")
        .status(EnableStatus.ENABLED)
        .sex(Teacher.SexEnum.F)
        .birthDate(LocalDate.parse("1990-01-04"))
        .entranceDatetime(Instant.parse("2021-10-09T08:28:24Z"))
        .address("Adr 5");
  }

  public static Course course1() {
    return new Course()
        .id(COURSE1_ID)
        .code("PROG1")
        .credits(6)
        .totalHours(20)
        .mainTeacher(teacher2())
        .name("Algorithmics");
  }

  public static Course course2() {
    return new Course()
        .id(COURSE2_ID)
        .code("PROG3")
        .credits(6)
        .totalHours(20)
        .mainTeacher(teacher1())
        .name("Advanced OOP");
  }

  public static Course course3() {
    return new Course()
        .id(COURSE3_ID)
        .code("WEB1")
        .credits(4)
        .totalHours(16)
        .mainTeacher(teacher2())
        .name("Web Interface");
  }


  public static Course course4() {
    return new Course()
        .id(COURSE4_ID)
        .code("WEB2")
        .credits(6)
        .totalHours(18)
        .mainTeacher(teacher3())
        .name("Web Application");
  }

  public static Course course5() {
    return new Course()
        .id(COURSE5_ID)
        .code("MGT1")
        .credits(5)
        .totalHours(12)
        .mainTeacher(teacher4())
        .name("Collaborative work");
  }

  public static Fee fee1() {
    return new Fee()
        .id(FEE1_ID)
        .studentId(STUDENT1_ID)
        .status(PAID)
        .type(TUITION)
        .totalAmount(5000)
        .remainingAmount(0)
        .comment("Comment")
        .updatedAt(Instant.parse("2023-02-08T08:30:24Z"))
        .creationDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
        .dueDatetime(Instant.parse("2021-12-08T08:25:24.00Z"));
  }

  public static Fee fee2() {
    return new Fee()
        .id(FEE2_ID)
        .studentId(STUDENT1_ID)
        .status(PAID)
        .type(HARDWARE)
        .totalAmount(5000)
        .remainingAmount(0)
        .comment("Comment")
        .updatedAt(Instant.parse("2023-02-08T08:30:24Z"))
        .creationDatetime(Instant.parse("2021-11-10T08:25:24.00Z"))
        .dueDatetime(Instant.parse("2021-12-10T08:25:24.00Z"));
  }

  public static Fee fee3() {
    return new Fee()
        .id(FEE3_ID)
        .studentId(STUDENT1_ID)
        .status(LATE)
        .type(TUITION)
        .totalAmount(5000)
        .remainingAmount(5000)
        .comment("Comment")
        .updatedAt(Instant.parse("2023-02-08T08:30:24Z"))
        .creationDatetime(Instant.parse("2022-12-08T08:25:24.00Z"))
        .dueDatetime(Instant.parse("2021-12-09T08:25:24.00Z"));
  }

  public static CreateFee creatableFee1() {
    return new CreateFee()
        .type(CreateFee.TypeEnum.TUITION)
        .totalAmount(5000)
        .comment("Comment")
        .dueDatetime(Instant.parse("2021-12-08T08:25:24.00Z"));
  }

  public static boolean isBefore(String a, String b) {
    return a.compareTo(b) < 0;
  }

  public static boolean isBefore(int a, int b) {
    return a < b;
  }

  public static boolean isValidUUID(String candidate) {
    try {
      UUID.fromString(candidate);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public static int anAvailableRandomPort() {
    try {
      return new ServerSocket(0).getLocalPort();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
