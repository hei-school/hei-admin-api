package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;
import static school.hei.haapi.integration.conf.TestUtils.EXAM1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT2_ID;

import java.util.List;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.User;

@Testcontainers
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
@Transactional
class DirtyGradeServiceTest extends FacadeITMockedThirdParties {
  @Autowired GradeService subject;
  @Autowired UserService userService;
  @Autowired ExamService examService;

  private List<Grade> someGrade(List<User> students, Exam exam) {
    return students.stream().map(student -> new Grade(null, student, exam, 18.2, null)).toList();
  }

  @Test
  void crupdate_grade_ok() {
    List<Grade> randomGrade =
        someGrade(List.of(userService.findById(STUDENT1_ID)), examService.getExamById(EXAM1_ID));
    List<Grade> randomGrades =
        someGrade(
            List.of(userService.findById(STUDENT1_ID), userService.findById(STUDENT2_ID)),
            examService.getExamById(EXAM1_ID));

    Grade savedGrade = subject.crupdateParticipantGrade(randomGrade).getFirst();
    List<Grade> savedGrades = subject.crupdateParticipantGrade(randomGrades);

    assertNotNull(savedGrade.getId());
    assertEquals(randomGrade.getFirst().getScore(), savedGrade.getScore());
    assertEquals(randomGrades.getFirst().getScore(), savedGrades.getFirst().getScore());
    assertEquals(randomGrades.get(1).getScore(), savedGrades.get(1).getScore());
  }
}
