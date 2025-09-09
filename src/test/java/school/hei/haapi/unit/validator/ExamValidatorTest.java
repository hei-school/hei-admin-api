package school.hei.haapi.unit.validator;

import static java.time.Instant.now;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsDomainBadRequestException;

import java.util.List;
import org.apache.commons.lang3.math.Fraction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.validator.ExamValidator;

@ExtendWith(MockitoExtension.class)
class ExamValidatorTest {

  private final ExamValidator subject = new ExamValidator();

  private static Exam validExam() {
    return Exam.builder()
        .title("Math Exam")
        .courseAssignment(new CourseAssignment())
        .coefficientNumerator(1)
        .coefficientDenominator(2)
        .examinationDate(now())
        .build();
  }

  @Test
  void acceptSingleExam_WithValidExam_ShouldNotThrowException() {
    assertDoesNotThrow(() -> subject.accept(validExam()));
  }

  @Test
  void acceptListOfExams_WithValidExams_ShouldNotThrowException() {
    List<Exam> exams = List.of(validExam(), validExam());

    assertDoesNotThrow(() -> subject.accept(exams));
  }

  @Test
  void acceptSingleExam_WithNullTitle_ShouldThrowBadRequestException() {
    var exam = validExam();
    exam.setTitle(null);

    var exception = assertThrowsDomainBadRequestException(() -> subject.accept(exam));

    assertTrue(exception.getMessage().contains("Title is mandatory"));
  }

  @Test
  void acceptSingleExam_WithNullCourseAssignment_ShouldThrowBadRequestException() {
    var exam = validExam();
    exam.setCourseAssignment(null);

    var exception = assertThrowsDomainBadRequestException(() -> subject.accept(exam));

    assertTrue(exception.getMessage().contains("Awarded course is mandatory"));
  }

  @Test
  void acceptSingleExam_WithNullExaminationDate_ShouldThrowBadRequestException() {
    var exam = validExam();
    exam.setExaminationDate(null);

    var exception = assertThrowsDomainBadRequestException(() -> subject.accept(exam));

    assertTrue(exception.getMessage().contains("Examination date is mandatory"));
  }

  @Test
  void acceptSingleExam_WithMultipleViolations_ShouldThrowExceptionWithAllMessages() {
    var exam = new Exam();

    var exception = assertThrowsDomainBadRequestException(() -> subject.accept(exam));
    String message = exception.getMessage();
    assertTrue(message.contains("Title is mandatory"));
    assertTrue(message.contains("Awarded course is mandatory"));
    assertTrue(message.contains("Examination date is mandatory"));
    assertTrue(message.contains("Coefficient numerator or denominator cannot be null"));
  }

  @Test
  void acceptListOfExams_WithOneInvalidExam_ShouldThrowExceptionOnFirstInvalid() {
    var invalidExam = validExam();
    invalidExam.setTitle(null);

    List<Exam> exams = List.of(validExam(), invalidExam);

    var exception = assertThrowsDomainBadRequestException(() -> subject.accept(exams));

    assertTrue(exception.getMessage().contains("Title is mandatory"));
  }

  @Test
  void acceptListOfExams_WithEmptyList_ShouldNotThrowException() {
    assertDoesNotThrow(() -> subject.accept(emptyList()));
  }

  @Test
  void acceptListOfExams_WithAllInvalidExams_ShouldThrowExceptionOnFirstExam() {
    var invalidExam1 = validExam();
    invalidExam1.setTitle(null);

    var invalidExam2 = validExam();
    invalidExam2.setCourseAssignment(null);

    List<Exam> exams = List.of(invalidExam1, invalidExam2);

    var exception = assertThrowsDomainBadRequestException(() -> subject.accept(exams));

    assertTrue(exception.getMessage().contains("Title is mandatory"));
    assertFalse(exception.getMessage().contains("Awarded course is mandatory"));
  }

  @Test
  void acceptExam_WithInvalidCoefficient_ShouldThrowException() {
    var exam = validExam();
    exam.setCoefficientFraction(aFraction(0, -2));

    var exception = assertThrowsDomainBadRequestException(() -> subject.accept(exam));

    assertTrue(
        exception.getMessage().contains("Coefficient numerator can't be less than or equal to 0"));
  }

  @Test
  void acceptExam_WithNumeratorGreaterThanDenominator_ShouldThrowException() {
    var exam = validExam();
    exam.setCoefficientFraction(aFraction(3, 2));
    var exception = assertThrowsDomainBadRequestException(() -> subject.accept(exam));
    assertTrue(
        exception
            .getMessage()
            .contains("Coefficient numerator cannot be greater than coefficient denominator"));
  }

  private static Fraction aFraction(int numerator, int denominator) {
    return Fraction.getFraction(numerator, denominator);
  }
}
