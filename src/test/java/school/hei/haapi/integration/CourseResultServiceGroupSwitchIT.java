package school.hei.haapi.integration;

import static java.time.Instant.parse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.endpoint.rest.model.CourseResultStatus.VALIDATED;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L1;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L2;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L3;
import static school.hei.haapi.integration.testData.GradeTestData.createGrade;
import static school.hei.haapi.integration.testData.StudentTestData.tolojanahary;
import static school.hei.haapi.integration.testData.TeacherTestData.ryan;
import static school.hei.haapi.model.GroupFlow.GroupFlowType.JOIN;
import static school.hei.haapi.model.GroupFlow.GroupFlowType.LEAVE;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.CycleLevel;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.CourseAssignmentRepository;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.ExamRepository;
import school.hei.haapi.repository.GradeRepository;
import school.hei.haapi.repository.GroupFlowRepository;
import school.hei.haapi.repository.GroupRepository;
import school.hei.haapi.repository.PromotionRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.CourseResultService;
import school.hei.haapi.service.GradeResultService;

class CourseResultServiceGroupSwitchIT extends FacadeITMockedThirdParties {
  @Autowired private UserRepository userRepository;
  @Autowired private PromotionRepository promotionRepository;
  @Autowired private GroupRepository groupRepository;
  @Autowired private GroupFlowRepository groupFlowRepository;
  @Autowired private CourseRepository courseRepository;
  @Autowired private CourseAssignmentRepository courseAssignmentRepository;
  @Autowired private ExamRepository examRepository;
  @Autowired private GradeRepository gradeRepository;
  @Autowired private CourseResultService courseResultService;
  @Autowired private GradeResultService gradeResultService;

  private User student;
  private User teacher;
  private Promotion promotion;
  private Group j1;
  private Group j2;
  private Course webCourse;
  private Course progCourse;
  private Course secuCourse;
  private Course sysCourse;
  private CourseAssignment webAssignment;
  private CourseAssignment progAssignment;
  private CourseAssignment secuAssignment;
  private CourseAssignment sysAssignment;
  private Exam webExam;
  private Exam progExam;
  private Exam secuExam;
  private Exam sysExam;
  private Grade webGrade;
  private Grade progGrade;
  private Grade secuGrade;
  private Grade sysGrade;
  private List<GroupFlow> groupFlows;

  @BeforeEach
  void setUp() {
    student = tolojanahary();
    teacher = ryan();

    promotion =
        Promotion.builder()
            .id(UUID.randomUUID().toString())
            .name("Promotion 2023 (group switch IT)")
            .ref("PROMO-2023-SWITCH-" + UUID.randomUUID())
            .startDatetime(parse("2023-11-01T00:00:00Z"))
            .cycleLevel(CycleLevel.BACHELOR)
            .build();
    j1 = group("J1");
    j2 = group("J2");

    webCourse = course("WEB", L1);
    progCourse = course("PROG", L2);
    secuCourse = course("SECU", L2);
    sysCourse = course("SYS", L3);

    webAssignment = assignment(webCourse, j1);
    progAssignment = assignment(progCourse, j2);
    secuAssignment = assignment(secuCourse, j1);
    sysAssignment = assignment(sysCourse, j1);

    webExam = exam(webAssignment, "2024-06-01T00:00:00Z");
    progExam = exam(progAssignment, "2024-12-01T00:00:00Z");
    secuExam = exam(secuAssignment, "2025-05-01T00:00:00Z");
    sysExam = exam(sysAssignment, "2026-01-01T00:00:00Z");

    webGrade = createGrade(student, webExam, 14.);
    progGrade = createGrade(student, progExam, 16.);
    secuGrade = createGrade(student, secuExam, 12.);
    sysGrade = createGrade(student, sysExam, 18.);

    groupFlows =
        List.of(
            groupFlow(j1, JOIN, "2023-11-05T00:00:00Z"),
            groupFlow(j1, LEAVE, "2024-10-01T00:00:00Z"),
            groupFlow(j2, JOIN, "2024-11-10T00:00:00Z"),
            groupFlow(j2, LEAVE, "2025-02-01T00:00:00Z"),
            groupFlow(j1, JOIN, "2025-02-01T00:00:00Z"));

    userRepository.saveAll(List.of(student, teacher));
    promotionRepository.save(promotion);
    groupRepository.saveAll(List.of(j1, j2));
    courseRepository.saveAll(List.of(webCourse, progCourse, secuCourse, sysCourse));
    courseAssignmentRepository.saveAll(
        List.of(webAssignment, progAssignment, secuAssignment, sysAssignment));
    examRepository.saveAll(List.of(webExam, progExam, secuExam, sysExam));
    gradeRepository.saveAll(List.of(webGrade, progGrade, secuGrade, sysGrade));
    groupFlowRepository.saveAll(groupFlows);
  }

  @AfterEach
  void tearDown() {
    gradeRepository.deleteAllInBatch(List.of(webGrade, progGrade, secuGrade, sysGrade));
    examRepository.deleteAllInBatch(List.of(webExam, progExam, secuExam, sysExam));
    courseAssignmentRepository.deleteAllInBatch(
        List.of(webAssignment, progAssignment, secuAssignment, sysAssignment));
    courseRepository.deleteAllInBatch(List.of(webCourse, progCourse, secuCourse, sysCourse));
    groupFlowRepository.deleteAllInBatch(groupFlows);
    groupRepository.deleteAllInBatch(List.of(j1, j2));
    promotionRepository.deleteAllInBatch(List.of(promotion));
    userRepository.deleteAllInBatch(List.of(student, teacher));
  }

  private Group group(String name) {
    return Group.builder()
        .id(UUID.randomUUID().toString())
        .name(name)
        .ref(UUID.randomUUID().toString())
        .promotion(promotion)
        .build();
  }

  private Course course(String name, StudentLevel level) {
    return Course.builder()
        .id(UUID.randomUUID().toString())
        .code(name + "-" + UUID.randomUUID())
        .name(name)
        .credits(6)
        .totalHours(60)
        .studentLevel(level)
        .build();
  }

  private CourseAssignment assignment(Course course, Group group) {
    return CourseAssignment.builder()
        .id(UUID.randomUUID().toString())
        .course(course)
        .mainTeacher(teacher)
        .groups(List.of(group))
        .build();
  }

  private Exam exam(CourseAssignment assignment, String examinationDate) {
    return Exam.builder()
        .id(UUID.randomUUID().toString())
        .title(assignment.getCourse().getName() + " exam")
        .grades(new ArrayList<>())
        .examinationDate(parse(examinationDate))
        .courseAssignment(assignment)
        .coefficientNumerator(1)
        .coefficientDenominator(1)
        .build();
  }

  private GroupFlow groupFlow(Group group, GroupFlow.GroupFlowType type, String at) {
    return GroupFlow.builder()
        .id(UUID.randomUUID().toString())
        .student(student)
        .group(group)
        .groupFlowType(type)
        .flowDatetime(parse(at))
        .build();
  }

  @Test
  void l1_returns_only_the_web_course_from_the_first_j1_stint() {
    var results = courseResultService.getCourseResultsByStudentIdAndLevel(student.getId(), L1);

    assertEquals(1, results.size());
    assertEquals(webCourse.getId(), results.getFirst().getCourse().getId());
    assertEquals(14., results.getFirst().getWeightedAverage().doubleValue());
    assertEquals(VALIDATED, results.getFirst().getStatus());
  }

  @Test
  void l2_merges_the_prog_course_from_j2_with_the_secu_course_from_the_j1_comeback() {
    var results = courseResultService.getCourseResultsByStudentIdAndLevel(student.getId(), L2);

    assertEquals(2, results.size());
    var courseIds = results.stream().map(r -> r.getCourse().getId()).toList();
    assertTrue(courseIds.contains(progCourse.getId()), "missing PROG (from J2) in " + courseIds);
    assertTrue(courseIds.contains(secuCourse.getId()), "missing SECU (from J1) in " + courseIds);
    assertTrue(results.stream().allMatch(r -> VALIDATED.equals(r.getStatus())));

    var yearlyResult = gradeResultService.getYearlyResultByStudentIdAndByLevel(student.getId(), L2);
    assertEquals(14., yearlyResult.getWeightedAverage().doubleValue());
    assertEquals(12., yearlyResult.getObtainedCredits().doubleValue());
  }

  @Test
  void l3_returns_only_the_sys_course_from_the_second_j1_stint() {
    var results = courseResultService.getCourseResultsByStudentIdAndLevel(student.getId(), L3);

    assertEquals(1, results.size());
    assertEquals(sysCourse.getId(), results.getFirst().getCourse().getId());
    assertEquals(18., results.getFirst().getWeightedAverage().doubleValue());
    assertEquals(VALIDATED, results.getFirst().getStatus());
  }
}
