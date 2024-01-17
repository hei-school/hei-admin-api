package school.hei.haapi.integration.conf;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.LATE;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.PAID;
import static school.hei.haapi.endpoint.rest.model.Fee.TypeEnum.HARDWARE;
import static school.hei.haapi.endpoint.rest.model.Fee.TypeEnum.TUITION;
import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.function.Executable;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.AwardedCourse;
import school.hei.haapi.endpoint.rest.model.AwardedCourseExam;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.CreateAwardedCourse;
import school.hei.haapi.endpoint.rest.model.CreateFee;
import school.hei.haapi.endpoint.rest.model.CreateGrade;
import school.hei.haapi.endpoint.rest.model.CrupdateTeacher;
import school.hei.haapi.endpoint.rest.model.EnableStatus;
import school.hei.haapi.endpoint.rest.model.ExamDetail;
import school.hei.haapi.endpoint.rest.model.ExamInfo;
import school.hei.haapi.endpoint.rest.model.Fee;
import school.hei.haapi.endpoint.rest.model.Grade;
import school.hei.haapi.endpoint.rest.model.Group;
import school.hei.haapi.endpoint.rest.model.Manager;
import school.hei.haapi.endpoint.rest.model.Sex;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.endpoint.rest.model.StudentExamGrade;
import school.hei.haapi.endpoint.rest.model.StudentGrade;
import school.hei.haapi.endpoint.rest.model.Teacher;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.service.aws.S3Service;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;

public class TestUtils {

  public static final String STUDENT1_ID = "student1_id";
  public static final String STUDENT2_ID = "student2_id";
  public static final String STUDENT3_ID = "student3_id";
  public static final String TEACHER1_ID = "teacher1_id";
  public static final String TEACHER2_ID = "teacher2_id";
  public static final String TEACHER3_ID = "teacher3_id";
  public static final String TEACHER4_ID = "teacher4_id";
  public static final String MANAGER_ID = "manager1_id";
  public static final String GROUP1_ID = "group1_id";
  public static final String GROUP2_ID = "group2_id";
  public static final String FEE1_ID = "fee1_id";
  public static final String FEE2_ID = "fee2_id";
  public static final String FEE3_ID = "fee3_id";
  public static final String FEE4_ID = "fee4_id";
  public static final String FEE5_ID = "fee5_id";
  public static final String FEE6_ID = "fee6_id";
  public static final String PAYMENT1_ID = "payment1_id";
  public static final String PAYMENT2_ID = "payment2_id";
  public static final String PAYMENT4_ID = "payment4_id";
  public static final String COURSE1_ID = "course1_id";
  public static final String COURSE2_ID = "course2_id";
  public static final String COURSE3_ID = "course3_id";
  public static final String COURSE4_ID = "course4_id";
  public static final String AWARDED_COURSE1_ID = "awarded_course1_id";
  public static final String AWARDED_COURSE2_ID = "awarded_course2_id";
  public static final String AWARDED_COURSE3_ID = "awarded_course3_id";
  public static final String AWARDED_COURSE4_ID = "awarded_course4_id";
  public static final String EXAM1_ID = "exam1_id";
  public static final String EXAM2_ID = "exam2_id";
  public static final String EXAM3_ID = "exam3_id";
  public static final String EXAM4_ID = "exam4_id";
  public static final String EXAM5_ID = "exam5_id";
  public static final String GRADE1_ID = "grade1_id";
  public static final String GRADE2_ID = "grade2_id";
  public static final String GRADE3_ID = "grade3_id";
  public static final String GRADE4_ID = "grade4_id";
  public static final String GRADE5_ID = "grade5_id";
  public static final String GRADE6_ID = "grade6_id";
  public static final String GRADE7_ID = "grade7_id";
  public static final String BAD_TOKEN = "bad_token";
  public static final String STUDENT1_TOKEN = "student1_token";
  public static final String TEACHER1_TOKEN = "teacher1_token";
  public static final String MANAGER1_TOKEN = "manager1_token";
  public static final byte[] MANAGER1_PROFILE_PNG = "manager1.png".getBytes();

  public static ApiClient anApiClient(String token, int serverPort) {
    ApiClient client = new ApiClient();
    client.setScheme("http");
    client.setHost("localhost");
    client.setPort(serverPort);
    client.setRequestInterceptor(
        httpRequestBuilder -> httpRequestBuilder.header("Authorization", "Bearer " + token));
    return client;
  }

  public static void setUpCognito(CognitoComponent cognitoComponent) {
    when(cognitoComponent.getEmailByIdToken(BAD_TOKEN)).thenReturn(null);
    when(cognitoComponent.getEmailByIdToken(STUDENT1_TOKEN)).thenReturn("test+ryan@hei.school");
    when(cognitoComponent.getEmailByIdToken(TEACHER1_TOKEN)).thenReturn("test+teacher1@hei.school");
    when(cognitoComponent.getEmailByIdToken(MANAGER1_TOKEN)).thenReturn("test+manager1@hei.school");
  }

  public static void setUpS3Service(S3Service s3Service, Student user){
    String USER_REF = user.getRef();
    when(s3Service.getPresignedUrl(USER_REF, 180L)).thenReturn(USER_REF);
    when(s3Service.uploadObjectToS3Bucket(USER_REF, getMockedFileAsByte("img", ".png"))).thenReturn(USER_REF);
  }

  public static void setUpS3Service(S3Service s3Service, Teacher user) {
    String USER_REF = user.getRef();
    when(s3Service.uploadObjectToS3Bucket(USER_REF, MANAGER1_PROFILE_PNG)).thenReturn(USER_REF);
  }

  public static void setUpS3Service(S3Service s3Service, Manager user) {
    String USER_REF = user.getRef();
    when(s3Service.uploadObjectToS3Bucket(USER_REF, MANAGER1_PROFILE_PNG)).thenReturn(USER_REF);
  }

  public static void setUpEventBridge(EventBridgeClient eventBridgeClient) {
    when(eventBridgeClient.putEvents((PutEventsRequest) any()))
        .thenReturn(PutEventsResponse.builder().build());
  }

  public static void assertThrowsApiException(String expectedBody, Executable executable) {
    ApiException apiException = assertThrows(ApiException.class, executable);
    assertEquals(expectedBody, apiException.getResponseBody());
  }

  public static void assertThrowsForbiddenException(Executable executable) {
    ApiException apiException = assertThrows(ApiException.class, executable);
    String responseBody = apiException.getResponseBody();
    assertEquals(
        "{" + "\"type\":\"403 FORBIDDEN\"," + "\"message\":\"Access is denied\"}", responseBody);
  }

  public static byte[] getMockedFileAsByte(String fileName, String fileType) {
    try {
      Resource resource = new ClassPathResource("mock/"+fileName+fileType);
      File file = resource.getFile();
      return FileUtils.readFileToByteArray(file);
    }
    catch (IOException e) {
      throw new school.hei.haapi.model.exception.ApiException(SERVER_EXCEPTION, e.getMessage());
    }
  }

  public static CrupdateTeacher someCreatableTeacher() {
    return new CrupdateTeacher()
        .firstName("Some")
        .lastName("User")
        .email(randomUUID() + "@hei.school")
        .ref("TCR21-" + randomUUID())
        .phone("0332511129")
        .status(EnableStatus.ENABLED)
        .sex(Sex.M)
        .birthDate(LocalDate.parse("2000-01-01"))
        .entranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
        .address("Adr X");
  }

  public static CreateFee creatableFee1() {
    return new CreateFee()
        .type(CreateFee.TypeEnum.TUITION)
        .totalAmount(5000)
        .comment("Comment")
        .dueDatetime(Instant.parse("2021-12-08T08:25:24.00Z"));
  }

  public static Group createGroup() {
    return new Group()
        .name("Collaborative work like GWSP")
        .ref("created")
        .creationDatetime(Instant.parse("2021-11-08T08:25:24.00Z"));
  }

  public static Course createCourse(String code) {
    return new Course().code(code).name("Collaborative work like GWSP").credits(12).totalHours(5);
  }

  public static CreateAwardedCourse updateAwardedCourse1() {
    return new CreateAwardedCourse()
        .id(AWARDED_COURSE1_ID)
        .courseId("course1_id")
        .groupId("group1_id")
        .mainTeacherId("teacher1_id");
  }

  public static CreateAwardedCourse createAwardedCourse() {
    return new CreateAwardedCourse()
        .courseId("course2_id")
        .groupId("group2_id")
        .mainTeacherId("teacher2_id");
  }

  public static ExamInfo createExam() {
    return new ExamInfo()
        .coefficient(10)
        .title("createExam")
        .examinationDate(Instant.parse("2021-11-08T08:25:24.00Z"))
        .awardedCourseId(AWARDED_COURSE1_ID);
  }

  public static CreateGrade createGrade(String studentId, String examId, String awardedCourseId) {
    return new CreateGrade()
        .score(20.0)
        .studentId(studentId)
        .examId(examId)
        .awardedCourseId(awardedCourseId);
  }

  public static List<CrupdateTeacher> someCreatableTeacherList(int nbOfTeacher) {
    List<CrupdateTeacher> teacherList = new ArrayList<>();
    for (int i = 0; i < nbOfTeacher; i++) {
      teacherList.add(someCreatableTeacher());
    }
    return teacherList;
  }

  public static List<Group> someCreatableGroupList(int nbOfGroup) {
    List<Group> groupList = new ArrayList<>();
    for (int i = 0; i < nbOfGroup; i++) {
      groupList.add(createGroup());
    }
    return groupList;
  }

  public static List<Course> someCreatableCourseList(int nbOfCourse) {
    List<Course> courseList = new ArrayList<>();
    for (int i = 0; i < nbOfCourse; i++) {
      courseList.add(createCourse("ToAdd" + i));
    }
    return courseList;
  }

  public static List<CreateAwardedCourse> someCreatableCreateAwardedCourseList(
      int nbOfCreateAwardedCourse) {
    List<CreateAwardedCourse> createAwardedCourseList = new ArrayList<>();
    for (int i = 0; i < nbOfCreateAwardedCourse; i++) {
      createAwardedCourseList.add(createAwardedCourse());
    }
    return createAwardedCourseList;
  }

  public static List<ExamInfo> someCreatableExamInfoList(int nbOfExamInfo) {
    List<ExamInfo> examInfoList = new ArrayList<>();
    for (int i = 0; i < nbOfExamInfo; i++) {
      examInfoList.add(createExam());
    }
    return examInfoList;
  }

  public static Group group1() {
    return new Group()
        .id(GROUP1_ID)
        .name("Name of group one")
        .ref("GRP21001")
        .creationDatetime(Instant.parse("2021-11-08T08:25:24.00Z"));
  }

  public static Group group2() {
    return new Group()
        .id(GROUP2_ID)
        .name("Name of group two")
        .ref("GRP21002")
        .creationDatetime(Instant.parse("2021-11-08T08:30:24.00Z"));
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
        .sex(Sex.F)
        .birthDate(LocalDate.parse("1990-01-01"))
        .entranceDatetime(Instant.parse("2021-10-08T08:27:24.00Z"))
        .nic("")
        .birthPlace("")
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
        .sex(Sex.M)
        .birthDate(LocalDate.parse("1990-01-02"))
        .entranceDatetime(Instant.parse("2021-10-09T08:28:24Z"))
        .nic("")
        .birthPlace("")
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
        .sex(Sex.M)
        .birthDate(LocalDate.parse("1990-01-02"))
        .entranceDatetime(Instant.parse("2021-10-09T08:28:24Z"))
        .nic("")
        .birthPlace("")
        .address("Adr 4");
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
        .sex(Sex.F)
        .birthDate(LocalDate.parse("1990-01-04"))
        .entranceDatetime(Instant.parse("2021-10-09T08:28:24Z"))
        .nic("")
        .birthPlace("")
        .address("Adr 5");
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

  public static Course course1() {
    return new Course().id(COURSE1_ID).code("PROG1").credits(6).totalHours(20).name("Algorithmics");
  }

  public static Course course2() {
    return new Course().id(COURSE2_ID).code("PROG3").credits(6).totalHours(24).name("Advanced OOP");
  }

  public static Course course3() {
    return new Course()
        .id(COURSE3_ID)
        .code("IA2")
        .credits(null)
        .totalHours(null)
        .name("Implemented IA");
  }

  public static Course course4() {
    return new Course()
        .id(COURSE4_ID)
        .code("donne1")
        .credits(4)
        .totalHours(30)
        .name("relational data base");
  }

  public static AwardedCourse awardedCourse1() {
    return new AwardedCourse()
        .id(AWARDED_COURSE1_ID)
        .course(course1())
        .group(group1())
        .mainTeacher(teacher1());
  }

  public static AwardedCourse awardedCourse2() {
    return new AwardedCourse()
        .id(AWARDED_COURSE2_ID)
        .course(course1())
        .group(group1())
        .mainTeacher(teacher2());
  }

  public static AwardedCourse awardedCourse3() {
    return new AwardedCourse()
        .id(AWARDED_COURSE3_ID)
        .course(course1())
        .group(group2())
        .mainTeacher(teacher2());
  }

  public static AwardedCourse awardedCourse4() {
    return new AwardedCourse()
        .id(AWARDED_COURSE4_ID)
        .course(course2())
        .group(group1())
        .mainTeacher(teacher4());
  }

  public static ExamInfo exam1() {
    return new ExamInfo()
        .id(EXAM1_ID)
        .coefficient(2)
        .title("Algorithmics")
        .awardedCourseId("awarded_course1_id")
        .examinationDate(Instant.parse("2022-10-09T08:25:24Z"));
  }

  public static ExamInfo exam2() {
    return new ExamInfo()
        .id(EXAM2_ID)
        .coefficient(3)
        .title("Algorithmics final")
        .awardedCourseId("awarded_course1_id")
        .examinationDate(Instant.parse("2022-11-09T08:25:24Z"));
  }

  public static ExamInfo exam3() {
    return new ExamInfo()
        .id(EXAM3_ID)
        .coefficient(2)
        .title("Algorithmics")
        .awardedCourseId("awarded_course3_id")
        .examinationDate(Instant.parse("2022-10-09T08:25:24Z"));
  }

  public static ExamInfo exam4() {
    return new ExamInfo()
        .id(EXAM4_ID)
        .coefficient(3)
        .title("Algorithmics2")
        .awardedCourseId("awarded_course2_id")
        .examinationDate(Instant.parse("2022-11-09T08:25:24Z"));
  }

  public static ExamInfo exam5() {
    return new ExamInfo()
        .id(EXAM5_ID)
        .coefficient(1)
        .title("Prog2 final")
        .awardedCourseId("awarded_course4_id")
        .examinationDate(Instant.parse("2022-12-09T08:25:24Z"));
  }

  public static Grade grade1() {
    return new Grade().id(GRADE1_ID).score(8.0).createdAt(Instant.parse("2022-10-09T08:25:24Z"));
  }

  public static Grade grade2() {
    return new Grade().id(GRADE2_ID).score(5.0).createdAt(Instant.parse("2022-11-09T08:25:24Z"));
  }

  public static Grade grade3() {
    return new Grade().id(GRADE3_ID).score(11.0).createdAt(Instant.parse("2022-11-09T08:25:24Z"));
  }

  public static Grade grade4() {
    return new Grade().id(GRADE4_ID).score(15.0).createdAt(Instant.parse("2022-10-09T08:25:24Z"));
  }

  public static Grade grade5() {
    return new Grade().id(GRADE5_ID).score(12.0).createdAt(Instant.parse("2022-10-09T08:25:24Z"));
  }

  public static Grade grade6() {
    return new Grade().id(GRADE6_ID).score(18.0).createdAt(Instant.parse("2022-11-09T08:25:24Z"));
  }

  public static Grade grade7() {
    return new Grade().id(GRADE7_ID).score(20.0).createdAt(Instant.parse("2022-10-09T08:25:24Z"));
  }

  public static StudentExamGrade studentExamGrade1() {
    return new StudentExamGrade()
        .id(exam1().getId())
        .coefficient(exam1().getCoefficient())
        .title(exam1().getTitle())
        .examinationDate(exam1().getExaminationDate())
        .grade(grade1());
  }

  public static StudentExamGrade studentExamGrade2() {
    return new StudentExamGrade()
        .id(exam2().getId())
        .coefficient(exam2().getCoefficient())
        .title(exam2().getTitle())
        .examinationDate(exam2().getExaminationDate())
        .grade(grade2());
  }

  public static StudentExamGrade studentExamGrade3() {
    return new StudentExamGrade()
        .id(exam3().getId())
        .coefficient(exam3().getCoefficient())
        .title(exam3().getTitle())
        .examinationDate(exam3().getExaminationDate())
        .grade(grade3());
  }

  public static StudentExamGrade studentExamGrade4() {
    return new StudentExamGrade()
        .id(exam4().getId())
        .coefficient(exam4().getCoefficient())
        .title(exam4().getTitle())
        .examinationDate(exam4().getExaminationDate())
        .grade(grade4());
  }

  public static StudentExamGrade studentExamGrade5() {
    return new StudentExamGrade()
        .id(exam5().getId())
        .coefficient(exam5().getCoefficient())
        .title(exam5().getTitle())
        .examinationDate(exam5().getExaminationDate())
        .grade(grade5());
  }

  public static StudentGrade studentGrade1() {
    return new StudentGrade()
        .id(STUDENT1_ID)
        .firstName("Ryan")
        .lastName("Andria")
        .ref("STD21001")
        .email("test+ryan@hei.school")
        .grade(grade1());
  }

  public static StudentGrade studentGrade2() {
    return new StudentGrade()
        .id(STUDENT1_ID)
        .firstName("Ryan")
        .lastName("Andria")
        .ref("STD21001")
        .email("test+ryan@hei.school")
        .grade(grade2());
  }

  public static StudentGrade studentGrade3() {
    return new StudentGrade()
        .id(STUDENT1_ID)
        .firstName("Ryan")
        .lastName("Andria")
        .ref("STD21001")
        .email("test+ryan@hei.school")
        .grade(grade3());
  }

  public static StudentGrade studentGrade4() {
    return new StudentGrade()
        .id(STUDENT1_ID)
        .firstName("Ryan")
        .lastName("Andria")
        .ref("STD21001")
        .email("test+ryan@hei.school")
        .grade(grade4());
  }

  public static StudentGrade studentGrade5() {
    return new StudentGrade()
        .id(STUDENT1_ID)
        .firstName("Ryan")
        .lastName("Andria")
        .ref("STD21001")
        .email("test+ryan@hei.school")
        .grade(grade5());
  }

  public static StudentGrade studentGrade6() {
    return new StudentGrade()
        .id(STUDENT2_ID)
        .firstName("Two")
        .lastName("Student")
        .ref("STD21002")
        .email("test+student2@hei.school")
        .grade(grade6());
  }

  public static StudentGrade studentGrade7() {
    return new StudentGrade()
        .id(STUDENT3_ID)
        .firstName("Three")
        .lastName("Student")
        .ref("STD21003")
        .email("test+student3@hei.school")
        .grade(grade7());
  }

  public static ExamDetail examDetail1() {
    return new ExamDetail()
        .id(exam1().getId())
        .title(exam1().getTitle())
        .examinationDate(exam1().getExaminationDate())
        .coefficient(exam1().getCoefficient())
        .participants(List.of(studentGrade1(), studentGrade7()));
  }

  public static AwardedCourseExam awardedCourseExam1() {
    return new AwardedCourseExam()
        .id(AWARDED_COURSE1_ID)
        .mainTeacher(awardedCourse1().getMainTeacher())
        .course(awardedCourse1().getCourse())
        .group(awardedCourse1().getGroup())
        .exams(List.of(studentExamGrade1(), studentExamGrade2()));
  }

  public static AwardedCourseExam awardedCourseExam2() {
    return new AwardedCourseExam()
        .id(AWARDED_COURSE2_ID)
        .mainTeacher(awardedCourse2().getMainTeacher())
        .course(awardedCourse2().getCourse())
        .group(awardedCourse2().getGroup())
        .exams(List.of(studentExamGrade4()));
  }

  public static AwardedCourseExam awardedCourseExam3() {
    return new AwardedCourseExam()
        .id(AWARDED_COURSE3_ID)
        .mainTeacher(awardedCourse3().getMainTeacher())
        .course(awardedCourse3().getCourse())
        .group(awardedCourse3().getGroup())
        .exams(List.of(studentExamGrade3()));
  }

  public static AwardedCourseExam awardedCourseExam4() {
    return new AwardedCourseExam()
        .id(AWARDED_COURSE4_ID)
        .mainTeacher(awardedCourse4().getMainTeacher())
        .course(awardedCourse4().getCourse())
        .group(awardedCourse4().getGroup())
        .exams(List.of(studentExamGrade5()));
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