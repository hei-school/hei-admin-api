package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;
import static school.hei.haapi.integration.conf.TestUtils.EXAM1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT2_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT3_ID;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.repository.GradeRepository;

@Testcontainers
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
class DirtyGradeServiceTest extends FacadeITMockedThirdParties {
  @Autowired GradeService subject;
  @Autowired UserService userService;
  @Autowired ExamService examService;
  @MockBean GradeRepository gradeRepository;

  private List<Grade> someGrade(List<User> students, Exam exam) {
    return students.stream().map(student -> new Grade(null, student, exam, 18.2, null)).toList();
  }

  @Test
  void crupdate_grades_ok() {
    List<Grade> randomGrades =
        someGrade(
            List.of(userService.findById(STUDENT1_ID), userService.findById(STUDENT2_ID)),
            examService.getExamById(EXAM1_ID));
    when(gradeRepository.saveAll(randomGrades)).thenReturn(randomGrades);
    randomGrades.forEach(
        grade -> {
          when(gradeRepository.findByExamIdAndStudentId(
                  grade.getExam().getId(), grade.getStudent().getId()))
              .thenReturn(Optional.of(grade));
        });

    List<Grade> savedGrades = subject.crupdateParticipantGrade(randomGrades);

    assertEquals(randomGrades.getFirst().getScore(), savedGrades.getFirst().getScore());
    assertEquals(randomGrades.get(1).getScore(), savedGrades.get(1).getScore());
  }

  @Test
  void crupdate_grade_ok() {
    List<Grade> randomGrade =
        someGrade(List.of(userService.findById(STUDENT1_ID)), examService.getExamById(EXAM1_ID));
    when(gradeRepository.saveAll(randomGrade)).thenReturn(randomGrade);
    randomGrade.forEach(
        grade -> {
          when(gradeRepository.findByExamIdAndStudentId(
                  grade.getExam().getId(), grade.getStudent().getId()))
              .thenReturn(Optional.of(grade));
        });

    Grade savedGrade = subject.crupdateParticipantGrade(randomGrade).getFirst();

    assertEquals(randomGrade.getFirst().getScore(), savedGrade.getScore());
  }

  @Test
  void crupdate_grade_of_student_not_in_exam_ko() {
    List<Grade> randomGrade =
        someGrade(List.of(userService.findById(STUDENT3_ID)), examService.getExamById(EXAM1_ID));

    assertThrows(BadRequestException.class, () -> subject.crupdateParticipantGrade(randomGrade));
  }
}
