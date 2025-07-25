package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L1;

import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.YearlyResult;
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
    var student1 = User.builder().ref("STD22075").build();
    var mgt1Grade =
        Grade.builder().score(17.75).exam(Exam.builder().coefficient(4).build()).build();
    var prog1Grade =
        Grade.builder().score(13.59).exam(Exam.builder().coefficient(3).build()).build();
    var donnees1Grade =
        Grade.builder().score(15.4375).exam(Exam.builder().coefficient(4).build()).build();
    var web1Grade =
        Grade.builder().score(18.75).exam(Exam.builder().coefficient(6).build()).build();
    var sys1Grade = Grade.builder().score(13.).exam(Exam.builder().coefficient(6).build()).build();
    var lv1Grade = Grade.builder().score(13.91).exam(Exam.builder().coefficient(4).build()).build();

    YearlyResult result = gradeResultService.getLeveledYearlyResultByStudentId(L1, "STD22075");

    assertEquals(L1, result.getLevel());
    assertEquals(30., result.getObtainedCredits().doubleValue());
    assertEquals(6, result.getCourseResults().size());
    assertEquals(15.35, result.getWeightedAverage().doubleValue());
  }
}
