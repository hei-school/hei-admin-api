package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static school.hei.haapi.integration.test_data.CourseAssignmentTestData.createCourseAssignment;
import static school.hei.haapi.integration.test_data.CourseTestData.prog1;
import static school.hei.haapi.integration.test_data.ExamTestData.createExam;
import static school.hei.haapi.integration.test_data.GroupTestData.createGroupFlowAt;
import static school.hei.haapi.integration.test_data.GroupTestData.g1;
import static school.hei.haapi.integration.test_data.GroupTestData.g2;
import static school.hei.haapi.integration.test_data.TeacherTestData.toky;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.rest.model.ExamGradeStats;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.test_data.StudentTestData;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.dao.GradeDao;

class GradeServiceTest extends FacadeITMockedThirdParties {
  @MockBean private GradeDao gradeDaoMock;
  @Autowired private GradeService subject;
  private User studentAxel;
  private Course prog1;
  private Group g1;
  private Group g2;

  @BeforeEach
  void setUp() {
    studentAxel = StudentTestData.axel();
    prog1 = prog1();
    g1 = g1();
    g2 = g2();
  }

  @Test
  void correct_exam_grade_stats() {
    var badGrade = Grade.builder().score(10.).build();
    var goodGrade1 = Grade.builder().score(19.).build();
    var goodGrade2 = Grade.builder().score(17.5).build();
    when(gradeDaoMock.getGradesByExamId(anyString()))
        .thenReturn(List.of(badGrade, goodGrade1, goodGrade2));

    ExamGradeStats examGradeStats = subject.getExamGradeStats("random exam");

    assertEquals(15.5, examGradeStats.getAverage());
  }

  @Test
  void exam_with_no_grades() {
    String examId = "random exam";
    Exception exception =
        assertThrows(NotFoundException.class, () -> subject.getExamGradeStats(examId));

    assertEquals("Exam with id " + examId + " do not have a score", exception.getMessage());
  }

  @Test
  void createParticipantGrade_forPreviousGroup_ok() {
    var joinG1 = createGroupFlowAt(studentAxel, g1, Instant.parse("2026-01-13T08:00:00Z"));
    var joinG2 = createGroupFlowAt(studentAxel, g2, Instant.parse("2026-01-20T08:00:00Z"));
    studentAxel.setGroupFlows(List.of(joinG1, joinG2));
    var assignProg1ToG1 = createCourseAssignment(prog1(), toky(), List.of(g1));
    var prog1Exam = createExam(Instant.now(), assignProg1ToG1);
    var toCreate =
        Grade.builder()
            .score(10.00)
            .student(studentAxel)
            .exam(prog1Exam)
            .creationDatetime(Instant.now())
            .build();

    assertEquals(toCreate, subject.checkGradeToCreate(toCreate));
  }

  @Test
  void createParticipantGrade_forUnanssignedStudent_ko() {
    var joinG1 = createGroupFlowAt(studentAxel, g1, Instant.parse("2026-01-13T08:00:00Z"));
    studentAxel.setGroupFlows(List.of(joinG1));
    var assignProg1ToG2 = createCourseAssignment(prog1(), toky(), List.of(g2));
    var prog1Exam = createExam(Instant.now(), assignProg1ToG2);
    var toCreate =
        Grade.builder()
            .score(10.00)
            .student(studentAxel)
            .exam(prog1Exam)
            .creationDatetime(Instant.now())
            .build();

    assertThrows(
        BadRequestException.class,
        () -> subject.checkGradeToCreate(toCreate),
        "Student with id "
            + studentAxel.getId()
            + " is not in exam "
            + prog1Exam.getId()
            + " assignment "
            + assignProg1ToG2.getId()
            + " group "
            + prog1Exam.getId());
  }

  @Test
  void createParticipantGrade_forGrouplessStudent_ko() {
    studentAxel.setGroupFlows(List.of());
    var assignProg1ToG1 = createCourseAssignment(prog1(), toky(), List.of(g1));
    var prog1Exam = createExam(Instant.now(), assignProg1ToG1);
    var toCreate =
        Grade.builder()
            .score(10.00)
            .student(studentAxel)
            .exam(prog1Exam)
            .creationDatetime(Instant.now())
            .build();

    assertThrows(
        BadRequestException.class,
        () -> subject.checkGradeToCreate(toCreate),
        "Student with id " + studentAxel.getId() + " is not in any group");
  }
}
