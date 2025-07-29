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
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.CourseCreditsSumZero;
import school.hei.haapi.repository.dao.CourseDao;
import school.hei.haapi.repository.dao.GradeDao;
import school.hei.haapi.service.utils.CourseResultUtils;

class GradeResultServiceTest {
  private final GradeDao gradeDao = mock();
  private final CourseDao courseDao = mock();
  private final GradeResultService gradeResultService =
      new GradeResultService(new CourseResultUtils(courseDao, gradeDao, new CourseMapper()));

  private final User student1 = User.builder().id("id").build();
  private final Grade mgt1Grade =
      Grade.builder().score(17.75).exam(Exam.builder().coefficient(1).build()).build();
  private final Grade prog1Grade =
      Grade.builder().score(13.59).exam(Exam.builder().coefficient(1).build()).build();
  private final Grade donnees1Grade =
      Grade.builder().score(15.4375).exam(Exam.builder().coefficient(1).build()).build();
  private final Grade web1Grade =
      Grade.builder().score(18.75).exam(Exam.builder().coefficient(1).build()).build();
  private final Grade sys1Grade =
      Grade.builder().score(13.).exam(Exam.builder().coefficient(1).build()).build();
  private final Grade lv1Grade =
      Grade.builder().score(13.91).exam(Exam.builder().coefficient(1).build()).build();
  private final Course mgt1Course = Course.builder().id("mgt1").credits(4).build();
  private final Course prog1Course = Course.builder().id("prog1").credits(6).build();
  private final Course donne1Course = Course.builder().id("donne1").credits(4).build();
  private final Course web1Course = Course.builder().id("web1").credits(6).build();
  private final Course sys1Course = Course.builder().id("sys1").credits(6).build();
  private final Course lv1Course = Course.builder().id("lv1").credits(4).build();

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

    when(courseDao.findByCriteria(any(), any(), any(), any(), any(), any(), any(), eq(L1), any()))
        .thenReturn(
            List.of(mgt1Course, prog1Course, donne1Course, web1Course, sys1Course, lv1Course));
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
    assertThrows(
        CourseCreditsSumZero.class,
        () -> gradeResultService.getLeveledYearlyResultByStudentId(M2, student1.getId()));
  }

  @Test
  void correct_result_result_summary() {
    ResultSummary result = gradeResultService.getStudentResultSummary(student1.getId());

    assertEquals(1, result.getYearlyResults().size());
    assertEquals(30., result.getObtainedCredits().doubleValue());
    assertEquals(15.347666666666667, result.getWeightedAverage().doubleValue());
  }
}
