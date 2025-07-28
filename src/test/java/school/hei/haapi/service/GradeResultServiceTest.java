package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L1;

import java.util.List;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.YearlyResult;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.dao.CourseDao;
import school.hei.haapi.repository.dao.GradeDao;

class GradeResultServiceTest {
  private GradeDao gradeDao = mock();
  private CourseDao courseDao = mock();
  private GradeResultService gradeResultService =
      new GradeResultService(courseDao, gradeDao, new CourseMapper());

  @Test
  void correct_result() {
    var student1 = User.builder().id("id").ref("STD22075").build();
    var mgt1Grade =
        Grade.builder().score(17.75).exam(Exam.builder().coefficient(1).build()).build();
    var prog1Grade =
        Grade.builder().score(13.59).exam(Exam.builder().coefficient(1).build()).build();
    var donnees1Grade =
        Grade.builder().score(15.4375).exam(Exam.builder().coefficient(1).build()).build();
    var web1Grade =
        Grade.builder().score(18.75).exam(Exam.builder().coefficient(1).build()).build();
    var sys1Grade = Grade.builder().score(13.).exam(Exam.builder().coefficient(1).build()).build();
    var lv1Grade = Grade.builder().score(13.91).exam(Exam.builder().coefficient(1).build()).build();
    var mgt1Course = Course.builder().id("mgt1").credits(4).build();
    var prog1Course = Course.builder().id("prog1").credits(6).build();
    var donne1Course = Course.builder().id("donne1").credits(4).build();
    var web1Course = Course.builder().id("web1").credits(6).build();
    var sys1Course = Course.builder().id("sys1").credits(6).build();
    var lv1Course = Course.builder().id("lv1").credits(4).build();
    when(courseDao.findByCriteria(any(), any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(
            List.of(mgt1Course, prog1Course, donne1Course, web1Course, sys1Course, lv1Course));
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

    YearlyResult result =
        gradeResultService.getLeveledYearlyResultByStudentId(L1, student1.getId());

    assertEquals(L1, result.getLevel());
    assertEquals(30., result.getObtainedCredits().doubleValue());
    assertEquals(6, result.getCourseResults().size());
    assertEquals(15.347666666666667, result.getWeightedAverage().doubleValue());
  }
}
