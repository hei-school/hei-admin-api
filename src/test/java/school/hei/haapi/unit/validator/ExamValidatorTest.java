<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 08b4e47 (feat: validate exam id in createStudentExamGrade)
package school.hei.haapi.unit.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static school.hei.haapi.integration.conf.TestUtils.AWARDED_COURSE1_ID;
import static school.hei.haapi.integration.conf.TestUtils.EXAM1_ID;
import static school.hei.haapi.integration.conf.TestUtils.EXAM2_ID;
import static school.hei.haapi.integration.conf.TestUtils.NOT_EXISTING_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT2_ID;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.model.CreateGrade;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.validator.ExamValidator;
import school.hei.haapi.service.ExamService;

class ExamValidatorTest {

  private ExamValidator examValidator;
  private ExamService examService;

  static CreateGrade createGrade1() {
    return new CreateGrade()
        .score(12.5)
        .studentId(STUDENT1_ID)
        .examId(EXAM1_ID)
        .awardedCourseId(AWARDED_COURSE1_ID);
  }

  static CreateGrade createGrade2() {
    return new CreateGrade()
        .score(12.5)
        .studentId(STUDENT2_ID)
        .examId(EXAM2_ID)
        .awardedCourseId(AWARDED_COURSE1_ID);
  }

  @BeforeEach
  void setUp() {
    examService = mock(ExamService.class);
    examValidator = new ExamValidator(examService);
  }

  @Test
  void validate_exam_id_in_path_ko() {
    List<CreateGrade> createGrades = new ArrayList<>();
    when(examService.examExistsById(NOT_EXISTING_ID)).thenReturn(false);

    BadRequestException exception =
        assertThrows(
            BadRequestException.class,
            () -> examValidator.validateExamId(NOT_EXISTING_ID, createGrades));

    assertTrue(exception.getMessage().contains("Exam with this id does not exist"));
  }

  @Test
  void validate_exam_id_in_payload_ko() {
    List<CreateGrade> createGrades = List.of(createGrade1(), createGrade2());
    when(examService.examExistsById(EXAM1_ID)).thenReturn(true);

    BadRequestException exception =
        assertThrows(
            BadRequestException.class, () -> examValidator.validateExamId(EXAM1_ID, createGrades));

    assertTrue(
        exception.getMessage().contains("Exam ids in the payload do not match the id in the URL"));
  }

  @Test
  void validate_exam_id_ok() {
    List<CreateGrade> gradesToCreate = List.of(createGrade1());
    when(examService.examExistsById(EXAM1_ID)).thenReturn(true);

    assertDoesNotThrow(() -> examValidator.validateExamId(EXAM1_ID, gradesToCreate));
  }
<<<<<<< HEAD
=======
package school.hei.haapi.unit.validator;public class ExamValidatorTest {
>>>>>>> 46959d7 (refactor: change createStudentExamGrade to return single ExamDetail)
=======
>>>>>>> 08b4e47 (feat: validate exam id in createStudentExamGrade)
}
