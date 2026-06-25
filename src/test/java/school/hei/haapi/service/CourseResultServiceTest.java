package school.hei.haapi.service;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L1;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;
import static school.hei.haapi.integration.test_data.CourseTestData.prog1;
import static school.hei.haapi.integration.test_data.CourseTestData.prog3;
import static school.hei.haapi.integration.test_data.CourseTestData.secu1;
import static school.hei.haapi.integration.test_data.GroupTestData.g1;
import static school.hei.haapi.integration.test_data.GroupTestData.g2;
import static school.hei.haapi.integration.test_data.StudentTestData.axel;
import static school.hei.haapi.integration.test_data.StudentTestData.tolojanahary;
import static school.hei.haapi.integration.test_data.TeacherTestData.ryan;
import static school.hei.haapi.model.GroupFlow.GroupFlowType.JOIN;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.CourseAssignmentRepository;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.GroupFlowRepository;
import school.hei.haapi.repository.GroupRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.dao.GradeDao;

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
  private Group group1;
  private Group group2;
  private GroupFlow tolojanaharyNewGroupFlow;
  private GroupFlow tolojanaharyOldGroupFlow;
  @MockBean private ExamService examService;
  @MockBean private GradeDao gradeDao;

  @BeforeEach
  void setUp() {
    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
    setUpTestData();
  }

  void setUpTestData() {
    now = Instant.now();
    studentAxel = axel();
    repeatingStudentTolojanahary = tolojanahary();
    teacherRyan = ryan();
    group1 = g1();
    group2 = g2();
    anL1Course = prog1();
    anL2Course = prog3();
    anL3Course = secu1();
    axelGroupFlow =
        GroupFlow.builder()
            .student(studentAxel)
            .group(group1)
            .groupFlowType(JOIN)
            .flowDatetime(now.minus(7, DAYS))
            .build();
    tolojanaharyOldGroupFlow =
        GroupFlow.builder()
            .student(repeatingStudentTolojanahary)
            .group(group1)
            .groupFlowType(JOIN)
            .flowDatetime(now.minus(365, DAYS))
            .build();
    tolojanaharyNewGroupFlow =
        GroupFlow.builder()
            .student(repeatingStudentTolojanahary)
            .group(group2)
            .groupFlowType(JOIN)
            .flowDatetime(now)
            .build();
    anL1Assignment =
        CourseAssignment.builder()
            .course(anL1Course)
            .groups(List.of(group1, group2))
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

    userRepository.saveAll(List.of(studentAxel, repeatingStudentTolojanahary, teacherRyan));
    groupRepository.saveAll(List.of(group1, group2));
    courseRepository.saveAll(List.of(anL1Course, anL2Course, anL3Course));
    courseAssignmentRepository.saveAll(List.of(anL1Assignment, anL2Assignment, anL3Assignment));
    groupFlowRepository.saveAll(
        List.of(axelGroupFlow, tolojanaharyOldGroupFlow, tolojanaharyNewGroupFlow));

    Exam examMock = mock(Exam.class);
    when(examMock.getExaminationDate()).thenReturn(now);
    when(examService.getExamsByCourseId(anL1Course.getId())).thenReturn(List.of(examMock));
    when(examService.getExamsByCourseId(anL2Course.getId())).thenReturn(List.of(examMock));
    when(examService.getExamsByCourseId(anL3Course.getId())).thenReturn(List.of(examMock));
    when(gradeDao.getStudentGradesByCourseId(any(), any())).thenReturn(List.of());
  }

  @AfterEach
  void tearDown() {
    courseAssignmentRepository.deleteAll(List.of(anL1Assignment, anL2Assignment, anL3Assignment));
    courseRepository.deleteAll(List.of(anL1Course, anL2Course, anL3Course));
    groupFlowRepository.deleteAll(
        List.of(axelGroupFlow, tolojanaharyOldGroupFlow, tolojanaharyNewGroupFlow));
    groupRepository.deleteAll(List.of(group1, group2));
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
                    courseAssignmentRepository.findAllByGroupId(group2.getId()).stream()
                        .map(courseAssignment -> courseAssignment.getCourse().getId())
                        .toList()
                        .contains(courseResult.getCourse().getId())));
  }
}
