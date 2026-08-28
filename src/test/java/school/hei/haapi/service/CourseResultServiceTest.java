package school.hei.haapi.service;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.endpoint.rest.model.CourseResultStatus.VALIDATED;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L1;
import static school.hei.haapi.integration.testData.CourseTestData.prog1;
import static school.hei.haapi.integration.testData.CourseTestData.prog3;
import static school.hei.haapi.integration.testData.CourseTestData.secu1;
import static school.hei.haapi.integration.testData.ExamTestData.createExam;
import static school.hei.haapi.integration.testData.GroupTestData.g1;
import static school.hei.haapi.integration.testData.GroupTestData.g2;
import static school.hei.haapi.integration.testData.GroupTestData.h1;
import static school.hei.haapi.integration.testData.StudentTestData.axel;
import static school.hei.haapi.integration.testData.StudentTestData.tolojanahary;
import static school.hei.haapi.integration.testData.TeacherTestData.ryan;
import static school.hei.haapi.model.GroupFlow.GroupFlowType.JOIN;
import static school.hei.haapi.model.GroupFlow.GroupFlowType.LEAVE;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.CourseAssignmentRepository;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.ExamRepository;
import school.hei.haapi.repository.GradeRepository;
import school.hei.haapi.repository.GroupFlowRepository;
import school.hei.haapi.repository.GroupRepository;
import school.hei.haapi.repository.UserRepository;

class CourseResultServiceTest extends FacadeITMockedThirdParties {
  @Autowired private CourseRepository courseRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private GroupRepository groupRepository;
  @Autowired private CourseAssignmentRepository courseAssignmentRepository;
  @Autowired private GroupFlowRepository groupFlowRepository;
  @Autowired private CourseResultService courseResultService;

  private Instant now;
  private User studentAxel;
  private User repeatingStudentTolojanahary;
  private User teacherRyan;
  private GroupFlow axelGroupFlow;
  private Course anL1Course;
  private Course anL2Course;
  private Course anL3Course;
  private CourseAssignment anL1Assignment;
  private CourseAssignment anL2Assignment;
  private CourseAssignment anL3Assignment;
  private CourseAssignment newL1Assignment;
  private Group group1;
  private Group group2;
  private Group group3;
  private GroupFlow tolojanaharyNewGroupFlow;
  private GroupFlow tolojanaharyJoinOldGroupFlow;
  private GroupFlow tolojanaharyLeaveOldGroupFlow;
  private Exam prog1Exam;
  private Exam groupHProg1Exam;
  private Exam prog3Exam;
  private Exam secu1Exam;
  private Grade repeatingYearGrade;
  @Autowired private ExamRepository examRepository;
  @Autowired private GradeRepository gradeRepository;

  @BeforeEach
  void setUp() {
    setUpTestData();
  }

  void setUpTestData() {
    now = Instant.now();
    studentAxel = axel();
    repeatingStudentTolojanahary = tolojanahary();
    teacherRyan = ryan();
    group1 = g1();
    group2 = g2();
    group3 = h1();
    anL1Course = prog1();
    anL2Course = prog3();
    anL3Course = secu1();
    axelGroupFlow =
        GroupFlow.builder()
            .student(studentAxel)
            .group(group1)
            .groupFlowType(JOIN)
            .flowDatetime(now.minus(365, DAYS))
            .build();
    tolojanaharyJoinOldGroupFlow =
        GroupFlow.builder()
            .student(repeatingStudentTolojanahary)
            .group(group1)
            .groupFlowType(JOIN)
            .flowDatetime(now.minus(365, DAYS))
            .build();
    tolojanaharyLeaveOldGroupFlow =
        GroupFlow.builder()
            .student(repeatingStudentTolojanahary)
            .group(group1)
            .groupFlowType(LEAVE)
            .flowDatetime(now.minus(50, DAYS))
            .build();
    tolojanaharyNewGroupFlow =
        GroupFlow.builder()
            .student(repeatingStudentTolojanahary)
            .group(group3)
            .groupFlowType(JOIN)
            .flowDatetime(now.minus(40, DAYS))
            .build();
    anL1Assignment =
        CourseAssignment.builder()
            .course(anL1Course)
            .groups(List.of(group1, group2))
            .mainTeacher(teacherRyan)
            .build();
    newL1Assignment =
        CourseAssignment.builder()
            .course(anL1Course)
            .groups(List.of(group3))
            .mainTeacher(teacherRyan)
            .build();
    anL2Assignment =
        CourseAssignment.builder()
            .course(anL2Course)
            .groups(List.of(group1, group2))
            .mainTeacher(teacherRyan)
            .build();
    anL3Assignment =
        CourseAssignment.builder()
            .course(anL3Course)
            .groups(List.of(group1, group2))
            .mainTeacher(teacherRyan)
            .build();

    prog1Exam = createExam(now.minus(200, DAYS), anL1Assignment);
    groupHProg1Exam = createExam(now.minus(10, DAYS), newL1Assignment);
    prog3Exam = createExam(now.minus(200, DAYS), anL2Assignment);
    secu1Exam = createExam(now.minus(200, DAYS), anL3Assignment);
    repeatingYearGrade =
        Grade.builder()
            .student(repeatingStudentTolojanahary)
            .exam(groupHProg1Exam)
            .score(12.)
            .build();

    userRepository.saveAll(List.of(studentAxel, repeatingStudentTolojanahary, teacherRyan));
    groupRepository.saveAll(List.of(group1, group2, group3));
    courseRepository.saveAll(List.of(anL1Course, anL2Course, anL3Course));
    courseAssignmentRepository.saveAll(
        List.of(anL1Assignment, newL1Assignment, anL2Assignment, anL3Assignment));
    examRepository.saveAll(List.of(prog1Exam, prog3Exam, secu1Exam, groupHProg1Exam));
    gradeRepository.save(repeatingYearGrade);
    groupFlowRepository.saveAll(
        List.of(
            axelGroupFlow,
            tolojanaharyJoinOldGroupFlow,
            tolojanaharyLeaveOldGroupFlow,
            tolojanaharyNewGroupFlow));
  }

  @AfterEach
  void tearDown() {
    courseAssignmentRepository.deleteAll(
        List.of(anL1Assignment, newL1Assignment, anL2Assignment, anL3Assignment));
    courseRepository.deleteAll(List.of(anL1Course, anL2Course, anL3Course));
    groupFlowRepository.deleteAll(
        List.of(
            axelGroupFlow,
            tolojanaharyJoinOldGroupFlow,
            tolojanaharyLeaveOldGroupFlow,
            tolojanaharyNewGroupFlow));
    groupRepository.deleteAll(List.of(group1, group2, group3));
    userRepository.deleteAll(List.of(studentAxel, repeatingStudentTolojanahary, teacherRyan));
  }

  @Test
  void getCourseResultsByStudentIdAndLevel_forPassingStudent_ok() {
    var actualCourseResults =
        courseResultService.getCourseResultsByStudentIdAndLevel(studentAxel.getId(), L1);

    assertFalse(actualCourseResults.isEmpty());
    assertTrue(
        actualCourseResults.stream()
            .allMatch(courseResult -> courseResult.getCourse().getLevel() == L1));
  }

  @Test
  void getCourseResultsByStudentIdAndLevel_forRepeatingStudent_returns_itsMostRecentGrade() {
    var actualCourseResults =
        courseResultService.getCourseResultsByStudentIdAndLevel(
            repeatingStudentTolojanahary.getId(), L1);

    assertFalse(actualCourseResults.isEmpty());
    assertTrue(actualCourseResults.stream().allMatch(r -> r.getCourse().getLevel() == L1));
    assertTrue(
        actualCourseResults.stream()
            .allMatch(
                courseResult ->
                    courseAssignmentRepository.findAllByGroupId(group3.getId()).stream()
                        .map(courseAssignment -> courseAssignment.getCourse().getId())
                        .toList()
                        .contains(courseResult.getCourse().getId())));
    assertEquals(12., actualCourseResults.getFirst().getWeightedAverage().doubleValue());
    assertEquals(VALIDATED, actualCourseResults.getFirst().getStatus());
  }
}
