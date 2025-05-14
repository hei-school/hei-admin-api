package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.rest.model.ExamGradeStats;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.Grade;
import school.hei.haapi.repository.dao.GradeDao;

class GradeServiceTest extends FacadeITMockedThirdParties {
  @MockBean private GradeDao gradeDaoMock;
  @Autowired private GradeService subject;

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
}
