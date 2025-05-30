package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static school.hei.haapi.integration.conf.TestUtils.COURSE1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.exam1;
import static school.hei.haapi.integration.conf.TestUtils.exam2;
import static school.hei.haapi.integration.conf.TestUtils.grade1;
import static school.hei.haapi.integration.conf.TestUtils.grade2;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.rest.mapper.ExamMapper;
import school.hei.haapi.endpoint.rest.mapper.GradeMapper;
import school.hei.haapi.endpoint.rest.model.StudentCourseGradeStats;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.repository.ExamRepository;
import school.hei.haapi.repository.GradeRepository;

class ExamServiceTest extends FacadeITMockedThirdParties {
  @Autowired private ExamService subject;
  @MockBean private ExamRepository examRepository;
  @Autowired private ExamMapper examMapper;
  @MockBean private GradeRepository gradeRepository;
  @Autowired private GradeMapper gradeMapper;

  @Test
  void student_get_grade_for_each_exams_in_cours() {
    var exam1 = examMapper.toDomain(exam1(), null);
    var exam2 = examMapper.toDomain(exam2(), null);
    var grade1 = gradeMapper.toDomain(grade1());
    var grade2 = gradeMapper.toDomain(grade2());
    grade1.setExam(exam1);
    grade2.setExam(exam2);
    when(examRepository.findExamsByCourseId(anyString())).thenReturn(List.of(exam1, exam2));
    when(gradeRepository.findGradesByCourseIdAndStudentId(anyString(), anyString()))
        .thenReturn(List.of(grade1, grade2));

    StudentCourseGradeStats studentCourseGradeStats =
        subject.getStudentCourseGradeStats(COURSE1_ID, STUDENT1_ID);

    assertEquals(6.2, studentCourseGradeStats.getAverage());
  }
}
