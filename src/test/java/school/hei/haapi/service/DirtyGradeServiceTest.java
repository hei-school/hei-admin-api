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

import com.github.javafaker.Faker;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.rest.mapper.GradeMapper;
import school.hei.haapi.endpoint.rest.model.StudentGrade;
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
import school.hei.haapi.model.notEntity.UpdateGrade;
import school.hei.haapi.repository.CourseAssignmentRepository;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.ExamRepository;
import school.hei.haapi.repository.GroupFlowRepository;
import school.hei.haapi.repository.GroupRepository;
import school.hei.haapi.repository.UserRepository;

@Testcontainers
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
class DirtyGradeServiceTest extends FacadeITMockedThirdParties {
  @Autowired GradeService subject;
  @Autowired ExamParticipantService examParticipantService;
  @Autowired UserRepository userRepository;
  @Autowired GroupRepository groupRepository;
  @Autowired GroupFlowRepository groupFlowRepository;
  @Autowired CourseRepository courseRepository;
  @Autowired CourseAssignmentRepository courseAssignmentRepository;
  @Autowired ExamRepository examRepository;
  @Autowired GradeMapper gradeMapper;
  @MockBean private EventProducer eventProducer;
  private final Faker faker = new Faker();

  private User studentAxel;
  private User studentTolojanahary;
  private Course courseProg1;
  private User teacherToky;
  private Exam exam1Prog1;
  private CourseAssignment assignProg1ToToky;
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

    groupRepository.save(groupG1);
    userRepository.saveAll(List.of(studentAxel, studentTolojanahary, teacherToky));
    courseRepository.save(courseProg1);
    groupFlowsAxel = createGroupFlow(studentAxel, groupG1);
    groupFlowsTolojanahary = createGroupFlow(studentTolojanahary, groupG1);
    groupFlowRepository.saveAll(List.of(groupFlowsAxel, groupFlowsTolojanahary));
    assignProg1ToToky = createCourseAssignment(courseProg1, teacherToky, List.of(groupG1));
    courseAssignmentRepository.save(assignProg1ToToky);
    exam1Prog1 = createExam(Instant.parse("2025-07-22T10:15:30Z"), assignProg1ToToky);
    examRepository.save(exam1Prog1);

    studentAxel.getGroupFlows().add(groupFlowsAxel);
    studentTolojanahary.getGroupFlows().add(groupFlowsTolojanahary);
    gradesExam1Prog1 = someGrade(List.of(studentAxel, studentTolojanahary), exam1Prog1);
    examRepository.save(exam1Prog1);

    groupIds.add(groupG1.getId());
    studentIds.addAll(List.of(studentAxel.getId(), studentTolojanahary.getId()));
    groupFlowIds.addAll(List.of(groupFlowsAxel.getId(), groupFlowsTolojanahary.getId()));
    teacherIds.add(teacherToky.getId());
    courseIds.add(courseProg1.getId());
    courseAssignmentIds.add(assignProg1ToToky.getId());
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
    BoundedPageSize pageSize = new BoundedPageSize(100);
    List<Grade> createdGrades = subject.createParticipantGrade(gradesExam1Prog1);
    List<StudentGrade> restCreatedGrades =
        createdGrades.stream().map(grade1 -> gradeMapper.toRestStudentGrade(grade1)).toList();

    assertEquals(2, createdGrades.size());
    assertNotNull(createdGrades.getFirst().getId());
    assertTrue(
        examParticipantService
            .getExamParticipantsGrade(exam1Prog1.getId(), page, pageSize, null)
            .containsAll(restCreatedGrades));
    assertEquals(gradesExam1Prog1.getFirst(), createdGrades.getFirst());
    assertEquals(gradesExam1Prog1.get(1), createdGrades.get(1));

    var rectifiedGrades =
        gradesExam1Prog1.stream()
            .map(
                grade -> {
                  var updatedGrade =
                      grade.toBuilder().score(faker.number().randomDouble(2, 0, 20)).build();
                  return new UpdateGrade(
                      updatedGrade,
                      updatedGrade.getStudent(),
                      "Rectification for student %s".formatted(studentAxel.getRef()),
                      exam1Prog1);
                })
            .toList();

    List<Grade> updatedGrades = subject.updateParticipantGrade(rectifiedGrades);
    List<StudentGrade> restUpdatedGrades =
        updatedGrades.stream().map(gradeMapper::toRestStudentGrade).toList();

    assertEquals(2, updatedGrades.size());
    assertNotNull(updatedGrades.getFirst().getId());
    assertTrue(
        examParticipantService
            .getExamParticipantsGrade(exam1Prog1.getId(), page, pageSize, null)
            .containsAll(restUpdatedGrades));
    assertEquals(rectifiedGrades.getFirst().grade(), updatedGrades.getFirst());
    assertEquals(rectifiedGrades.get(1).grade(), updatedGrades.get(1));
  }

  @Test
  void filter_grades_by_student_ref_ok() {
    subject.createParticipantGrade(gradesExam1Prog1);

    var examGrades =
        examParticipantService.getExamParticipantsGrade(
            exam1Prog1.getId(), null, null, studentAxel.getRef());

    assertEquals(1, examGrades.size());
    var studentGrade = examGrades.getFirst();
    assertEquals(studentAxel.getId(), studentGrade.getStudent().getId());
    assertEquals(gradesExam1Prog1.getFirst().getId(), studentGrade.getGrade().getId());
  }
}
