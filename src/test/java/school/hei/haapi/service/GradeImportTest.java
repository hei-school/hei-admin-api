package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.TestUtils.getMockedFile;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.file.bucket.BucketComponent;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.User;
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
  @MockBean BucketComponent bucketComponent;
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

  @BeforeEach
  void setUp() {
    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
    setUpCognito(cognitoComponentMock);
    setUpS3Service(fileService, student1());
    setUpTestData();
  }

  @Test
  void validate_import_student_grade_ok() {
    var importResult =
        subject.initStudentExamGradeImportFromXlsx(
            getMockedFile("test-grade-import", ".xlsx"), "exam1_id");
    assertEquals(7, importResult.getImportGradeStats().getTotalRows());
    assertEquals(6, importResult.getImportGradeStats().getInvalidRows());
    assertEquals(1, importResult.getImportGradeStats().getValidRows());
    assertNotNull(importResult.getInvalidGrades());
    assertEquals(
        "La note est supérieur à 20", importResult.getInvalidGrades().getFirst().getReason());
    assertEquals("La note est négative", importResult.getInvalidGrades().get(1).getReason());
    assertEquals("La note est null", importResult.getInvalidGrades().get(2).getReason());
    assertEquals(
        "La réference est null ou vide", importResult.getInvalidGrades().get(3).getReason());
    assertEquals(
        "La réference étudiant(e) est dupliquée, veuillez supprimer les autres pour ajouter une"
            + " note.",
        importResult.getInvalidGrades().get(4).getReason());
  }
}
