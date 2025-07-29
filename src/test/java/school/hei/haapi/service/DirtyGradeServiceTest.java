package school.hei.haapi.service;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;
import static school.hei.haapi.integration.test_data.CourseAssignmentTestData.createCourseAssignment;
import static school.hei.haapi.integration.test_data.CourseTestData.prog1;
import static school.hei.haapi.integration.test_data.ExamTestData.createExam;
import static school.hei.haapi.integration.test_data.GroupTestData.createGroupFlow;
import static school.hei.haapi.integration.test_data.GroupTestData.g1;
import static school.hei.haapi.integration.test_data.StudentTestData.axel;
import static school.hei.haapi.integration.test_data.StudentTestData.tolojanahary;
import static school.hei.haapi.integration.test_data.TeacherTestData.toky;

import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.CourseAssignmentRepository;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.ExamRepository;
import school.hei.haapi.repository.GradeRepository;
import school.hei.haapi.repository.GroupFlowRepository;
import school.hei.haapi.repository.GroupRepository;
import school.hei.haapi.repository.UserRepository;

@Testcontainers
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
class DirtyGradeServiceTest extends FacadeITMockedThirdParties {
  @Autowired GradeService subject;
  @Autowired UserRepository userRepository;
  @Autowired GroupRepository groupRepository;
  @Autowired GroupFlowRepository groupFlowRepository;
  @Autowired CourseRepository courseRepository;
  @Autowired CourseAssignmentRepository courseAssignmentRepository;
  @Autowired ExamRepository examRepository;
  @Autowired GradeRepository gradeRepository;

  private User studentAxel;
  private User studentTolojanahary;
  private Course courseProg1;
  private User teacherToky;
  private Exam exam1Prog1;
  private CourseAssignment assign_prog1_toToky;
  private Group groupG1;
  private List<Grade> gradesExam1Prog1;
  private GroupFlow groupFlowsAxel;
  private GroupFlow groupFlowsTolojanahary;
  private List<String> studentIds = new ArrayList<>();
  private List<String> teacherIds = new ArrayList<>();
  private List<String> groupIds = new ArrayList<>();
  private List<String> courseIds = new ArrayList<>();
  private List<String> courseAssignmentIds = new ArrayList<>();
  private List<String> examIds = new ArrayList<>();
  private List<String> gradeIds = new ArrayList<>();
  private List<String> groupFlowIds = new ArrayList<>();

  private void setUpTestData() {
    groupG1 = g1();
    studentAxel = axel();
    studentTolojanahary = tolojanahary();
    courseProg1 = prog1();
    teacherToky = toky();
    groupFlowsAxel = createGroupFlow(studentAxel, groupG1);
    groupFlowsTolojanahary = createGroupFlow(studentTolojanahary, groupG1);
    assign_prog1_toToky = createCourseAssignment(courseProg1, teacherToky, List.of(groupG1));
    exam1Prog1 = createExam(Instant.parse("2025-07-22T10:15:30Z"), assign_prog1_toToky);
    gradesExam1Prog1 = someGrade(List.of(studentAxel, studentTolojanahary), exam1Prog1);
    exam1Prog1.setGrades(gradesExam1Prog1);

    groupRepository.save(groupG1);
    userRepository.saveAll(List.of(studentAxel, studentTolojanahary, teacherToky));
    courseRepository.save(courseProg1);
    groupFlowsAxel = createGroupFlow(studentAxel, groupG1);
    groupFlowsTolojanahary = createGroupFlow(studentTolojanahary, groupG1);
    groupFlowRepository.saveAll(List.of(groupFlowsAxel, groupFlowsTolojanahary));
    assign_prog1_toToky = createCourseAssignment(courseProg1, teacherToky, List.of(groupG1));
    courseAssignmentRepository.save(assign_prog1_toToky);
    exam1Prog1 = createExam(Instant.parse("2025-07-22T10:15:30Z"), assign_prog1_toToky);
    examRepository.save(exam1Prog1);

    gradesExam1Prog1 = someGrade(List.of(studentAxel, studentTolojanahary), exam1Prog1);
    gradeRepository.saveAll(gradesExam1Prog1);
    exam1Prog1.setGrades(gradesExam1Prog1);
    examRepository.save(exam1Prog1);

    groupIds.add(groupG1.getId());
    studentIds.addAll(List.of(studentAxel.getId(), studentTolojanahary.getId()));
    groupFlowIds.addAll(List.of(groupFlowsAxel.getId(), groupFlowsTolojanahary.getId()));
    teacherIds.add(teacherToky.getId());
    courseIds.add(courseProg1.getId());
    courseAssignmentIds.add(assign_prog1_toToky.getId());
    examIds.add(exam1Prog1.getId());
    gradeIds.addAll(List.of(gradesExam1Prog1.get(0).getId(), gradesExam1Prog1.get(1).getId()));
  }

  private List<Grade> someGrade(List<User> students, Exam exam) {
    return students.stream()
        .map(student -> new Grade(randomUUID().toString(), student, exam, 18.2, null))
        .toList();
  }

  @BeforeEach
  void setUp() {
    setUpTestData();
  }

  @Test
  @Transactional
  void crupdate_grade_ok() {
    PageFromOne page = new PageFromOne(1);
    BoundedPageSize pageSize = new BoundedPageSize(20);
    List<Grade> savedGrades = subject.crupdateParticipantGrade(gradesExam1Prog1);

    assertEquals(2, savedGrades.size());
    assertNotNull(savedGrades.getFirst().getId());
    assertTrue(
        subject
            .getParticipantsGradeForExam(exam1Prog1.getId(), page, pageSize)
            .containsAll(savedGrades));
  }
}
