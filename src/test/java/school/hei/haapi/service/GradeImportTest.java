package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static school.hei.haapi.integration.conf.TestUtils.getMockedFile;
import static school.hei.haapi.integration.test_data.CourseAssignmentTestData.createCourseAssignment;
import static school.hei.haapi.integration.test_data.CourseTestData.prog1;
import static school.hei.haapi.integration.test_data.CourseTestData.prog2;
import static school.hei.haapi.integration.test_data.ExamTestData.createExam;
import static school.hei.haapi.integration.test_data.GroupTestData.createGroupFlow;
import static school.hei.haapi.integration.test_data.GroupTestData.g1;
import static school.hei.haapi.integration.test_data.GroupTestData.g2;
import static school.hei.haapi.integration.test_data.MonitorTestData.monitorOfAxel;
import static school.hei.haapi.integration.test_data.MonitorTestData.monitorOfTolojanahary;
import static school.hei.haapi.integration.test_data.StudentTestData.axel;
import static school.hei.haapi.integration.test_data.StudentTestData.tolojanahary;
import static school.hei.haapi.integration.test_data.TeacherTestData.toky;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.GradeImportEvent;
import school.hei.haapi.endpoint.rest.security.AuthProvider;
import school.hei.haapi.endpoint.rest.security.model.Principal;
import school.hei.haapi.file.bucket.BucketComponent;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.User;
import school.hei.haapi.model.dto.GradeImportDto;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.repository.CourseAssignmentRepository;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.ExamRepository;
import school.hei.haapi.repository.GradeRepository;
import school.hei.haapi.repository.GroupFlowRepository;
import school.hei.haapi.repository.GroupRepository;
import school.hei.haapi.repository.MonitoringStudentRepository;
import school.hei.haapi.repository.UserRepository;

public class GradeImportTest extends FacadeITMockedThirdParties {
  @Autowired private GradeService subject;
  @Autowired private GradeImportEventService gradeImportEventService;
  @MockBean Mailer mailer;
  @MockBean BucketComponent bucketComponent;
  @MockBean private EventProducer eventProducer;
  @Autowired private UserService userService;
  @Autowired private GradeRepository gradeRepository;
  @Autowired UserRepository userRepository;
  @Autowired CourseRepository courseRepository;
  @Autowired GroupFlowRepository groupFlowRepository;
  @Autowired GroupRepository groupRepository;
  @Autowired CourseAssignmentRepository courseAssignmentRepository;
  @Autowired MonitoringStudentRepository monitoringStudentRepository;
  @Autowired ExamRepository examRepository;
  private static User studentAxel;
  private static User studentTolojanahary;
  private User monitorOfAxel;
  private User monitorOfTolojanahary;
  private Course courseProg1;
  private Course courseProg2;
  private User teacherToky;
  private Exam exam1Prog1;
  private Exam exam2Prog1;
  private CourseAssignment assignProg1ToTokyForGroup;
  private CourseAssignment assignProg2ToTokyForGroup2;
  private Group groupG1;
  private Group groupG2;
  private GroupFlow groupFlowsAxel;
  private GroupFlow groupFlowsTolojanahary;
  private static Exam exam1Prog1Saved;

  @BeforeEach
  void setUpTestData() {
    groupG1 = g1();
    groupG2 = g2();
    studentAxel = axel();
    studentTolojanahary = tolojanahary();
    courseProg1 = prog1();
    courseProg2 = prog2();
    teacherToky = toky();
    assignProg1ToTokyForGroup = createCourseAssignment(courseProg1, teacherToky, List.of(groupG1));
    assignProg2ToTokyForGroup2 = createCourseAssignment(courseProg2, teacherToky, List.of(groupG2));

    monitorOfAxel = monitorOfAxel();
    monitorOfTolojanahary = monitorOfTolojanahary();
    studentAxel.setMonitors(List.of(monitorOfAxel));
    studentTolojanahary.setMonitors(List.of(monitorOfTolojanahary));
    groupFlowsAxel = createGroupFlow(studentAxel, groupG1);
    groupFlowsTolojanahary = createGroupFlow(studentTolojanahary, groupG1);

    exam1Prog1 = createExam(Instant.parse("2025-07-22T10:15:30Z"), assignProg1ToTokyForGroup);
    exam2Prog1 = createExam(Instant.parse("2025-09-22T10:15:30Z"), assignProg1ToTokyForGroup);

    groupRepository.saveAll(List.of(groupG1, groupG2));
    userRepository.saveAll(List.of(monitorOfAxel, monitorOfTolojanahary));
    userRepository.saveAll(List.of(studentAxel, studentTolojanahary));
    monitoringStudentRepository.saveMonitorFollowingStudents(
        monitorOfAxel.getId(), List.of(studentAxel.getId()));
    monitoringStudentRepository.saveMonitorFollowingStudents(
        monitorOfTolojanahary.getId(), List.of(studentTolojanahary.getId()));
    userRepository.saveAll(List.of(teacherToky));
    courseRepository.saveAll(List.of(courseProg1, courseProg2));
    groupFlowRepository.saveAll(List.of(groupFlowsAxel, groupFlowsTolojanahary));
    courseAssignmentRepository.saveAll(
        List.of(assignProg1ToTokyForGroup, assignProg2ToTokyForGroup2));
    exam1Prog1Saved = examRepository.save(exam1Prog1);
    examRepository.save(exam2Prog1);
  }

  @BeforeAll
  static void setUp() {
    mockStatic(AuthProvider.class);
    when(AuthProvider.getPrincipal()).thenReturn(mockPrincipal());
  }

  @Test
  void validate_import_student_grade_ok() {
    var importResult =
        subject.initStudentExamGradeImportFromXlsx(
            getMockedFile("test-grade-import", ".xlsx"), "exam1_id");
    assertEquals(2, importResult.getValidStudentExamGradeNumber());
  }

  @Test
  void validate_bad_student_import_ko() {
    assertThrows(
        BadRequestException.class,
        () ->
            subject.initStudentExamGradeImportFromXlsx(
                getMockedFile("test-bad-student-grade-import", ".xlsx"), "exam1_id"));
  }

  @Test
  void handle_grade_import_xlsx() {
    assertDoesNotThrow(() -> gradeImportEventService.accept(gradeImportEventMock()));
    var grade =
        subject.getGradeByExamIdAndStudentRef(exam1Prog1Saved.getId(), studentAxel.getRef());
    assertEquals(studentAxel.getRef(), grade.getStudent().getRef());
    assertEquals(exam1Prog1.getId(), grade.getExam().getId());
  }

  @Test
  void import_bad_grade_ko() {
    assertThrows(Exception.class, () -> gradeImportEventService.accept(badImportEvent()));
  }

  private static Principal mockPrincipal() {
    return new Principal(User.builder().email("test@email.com").build(), "huh!?");
  }

  private static GradeImportEvent gradeImportEventMock() {
    return GradeImportEvent.builder()
        .examId(exam1Prog1Saved.getId())
        .coordinatorEmail("test+manager1@hei.school")
        .grades(
            List.of(
                GradeImportDto.builder().ref(studentAxel.getRef()).score(12.5).build(),
                GradeImportDto.builder().ref(studentTolojanahary.getRef()).score(13.5).build()))
        .build();
  }

  private static GradeImportEvent badImportEvent() {
    return GradeImportEvent.builder()
        .examId("exam1_id")
        .coordinatorEmail("test+manager1@hei.school")
        .grades(List.of(GradeImportDto.builder().ref("STD21010").score(12.5).build()))
        .build();
  }
}
