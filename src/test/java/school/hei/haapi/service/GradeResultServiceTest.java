package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L1;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.M2;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.ResultSummary;
import school.hei.haapi.endpoint.rest.model.YearlyResult;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.CourseCreditsSumZero;
import school.hei.haapi.repository.dao.CourseAssignmentDao;
import school.hei.haapi.repository.dao.GradeDao;

class GradeResultServiceTest {
  private final GradeDao gradeDao = mock();
  private final CourseAssignmentDao courseAssignmentDao = mock();
  private final ExamService examService = mock();
  private final GradeResultService gradeResultService =
      new GradeResultService(
          new CourseResultService(courseAssignmentDao, gradeDao, new CourseMapper(), examService));

  private final User student1 = User.builder().id("id").build();
  private final Exam mgt1Exam = Exam.builder().id("mgt1 exam").coefficient(1).build();
  private final Exam prog1Exam = Exam.builder().id("prog1 exam").coefficient(1).build();
  private final Exam donnees1Exam = Exam.builder().id("donnees1 exam").coefficient(1).build();
  private final Exam web1Exam = Exam.builder().id("web1 exam").coefficient(1).build();
  private final Exam sys1Exam = Exam.builder().id("sys1 exam").coefficient(1).build();
  private final Exam lv1Exam = Exam.builder().id("lv1 exam").coefficient(1).build();
  private final Grade mgt1Grade = Grade.builder().score(17.75).exam(mgt1Exam).build();
  private final Grade prog1Grade = Grade.builder().score(13.59).exam(prog1Exam).build();
  private final Grade donnees1Grade = Grade.builder().score(15.4375).exam(donnees1Exam).build();
  private final Grade web1Grade = Grade.builder().score(18.75).exam(web1Exam).build();
  private final Grade sys1Grade = Grade.builder().score(13.).exam(sys1Exam).build();
  private final Grade lv1Grade = Grade.builder().score(13.91).exam(lv1Exam).build();
  private final Course mgt1Course = Course.builder().id("mgt1").credits(4).build();
  private final Course prog1Course = Course.builder().id("prog1").credits(6).build();
  private final Course donne1Course = Course.builder().id("donne1").credits(4).build();
  private final Course web1Course = Course.builder().id("web1").credits(6).build();
  private final Course sys1Course = Course.builder().id("sys1").credits(6).build();
  private final Course lv1Course = Course.builder().id("lv1").credits(4).build();
  private final CourseAssignment mgt1CourseAssignment =
      CourseAssignment.builder().id("mgt1 courseAssignment").course(mgt1Course).build();
  private final CourseAssignment prog1CourseAssignment =
      CourseAssignment.builder().id("prog1 courseAssignment").course(prog1Course).build();
  private final CourseAssignment donnee1CourseAssignment =
      CourseAssignment.builder().id("donnee1 courseAssignment").course(donne1Course).build();
  private final CourseAssignment web1CourseAssignment =
      CourseAssignment.builder().id("web1 courseAssignment").course(web1Course).build();
  private final CourseAssignment sys1CourseAssignment =
      CourseAssignment.builder().id("sys1 courseAssignment").course(sys1Course).build();
  private final CourseAssignment lv1CourseAssignment =
      CourseAssignment.builder().id("lv1 courseAssignment").course(lv1Course).build();

  @BeforeEach
  void setUp() {
    when(gradeDao.getStudentGradesByCourseId(mgt1Course.getId(), student1.getId()))
        .thenReturn(List.of(mgt1Grade));
    when(gradeDao.getStudentGradesByCourseId(prog1Course.getId(), student1.getId()))
        .thenReturn(List.of(prog1Grade));
    when(gradeDao.getStudentGradesByCourseId(donne1Course.getId(), student1.getId()))
        .thenReturn(List.of(donnees1Grade));
    when(gradeDao.getStudentGradesByCourseId(web1Course.getId(), student1.getId()))
        .thenReturn(List.of(web1Grade));
    when(gradeDao.getStudentGradesByCourseId(sys1Course.getId(), student1.getId()))
        .thenReturn(List.of(sys1Grade));
    when(gradeDao.getStudentGradesByCourseId(lv1Course.getId(), student1.getId()))
        .thenReturn(List.of(lv1Grade));

    when(examService.getExamsByCourseAssignmentId(mgt1CourseAssignment.getId()))
        .thenReturn(List.of(mgt1Exam));
    when(examService.getExamsByCourseAssignmentId(prog1CourseAssignment.getId()))
        .thenReturn(List.of(prog1Exam));
    when(examService.getExamsByCourseAssignmentId(donnee1CourseAssignment.getId()))
        .thenReturn(List.of(donnees1Exam));
    when(examService.getExamsByCourseAssignmentId(web1CourseAssignment.getId()))
        .thenReturn(List.of(web1Exam));
    when(examService.getExamsByCourseAssignmentId(sys1CourseAssignment.getId()))
        .thenReturn(List.of(sys1Exam));
    when(examService.getExamsByCourseAssignmentId(lv1CourseAssignment.getId()))
        .thenReturn(List.of(lv1Exam));

    when(courseAssignmentDao.findByCriteria(any(), any(), eq(L1), any()))
        .thenReturn(
            List.of(
                mgt1CourseAssignment,
                prog1CourseAssignment,
                donnee1CourseAssignment,
                web1CourseAssignment,
                sys1CourseAssignment,
                lv1CourseAssignment));
  }

  @Test
  void correct_result_yearly_result_L1() throws CourseCreditsSumZero {
    var targetLevel = L1;

    YearlyResult result =
        gradeResultService.getLeveledYearlyResultByStudentId(targetLevel, student1.getId());

    assertEquals(targetLevel, result.getLevel());
    assertEquals(30., result.getObtainedCredits().doubleValue());
    assertEquals(6, result.getCourseResults().size());
    assertEquals(15.347666666666667, result.getWeightedAverage().doubleValue());
  }

  @Test
  void correct_result_yearly_result_M2_empty_ko() {
    String studentId = student1.getId();

    assertThrows(
        CourseCreditsSumZero.class,
        () -> gradeResultService.getLeveledYearlyResultByStudentId(M2, studentId));
  }

  @Test
  void correct_result_result_summary() {
    ResultSummary result = gradeResultService.getStudentResultSummary(student1.getId());

    assertEquals(1, result.getYearlyResults().size());
    assertEquals(30., result.getObtainedCredits().doubleValue());
    assertEquals(15.3476666666666667, result.getWeightedAverage().doubleValue());
  }
}
