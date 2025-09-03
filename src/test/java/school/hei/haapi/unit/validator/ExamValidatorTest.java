package school.hei.haapi.unit.validator;

import static java.time.Instant.now;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.validator.ExamValidator;

@ExtendWith(MockitoExtension.class)
class ExamValidatorTest {

  private ExamValidator examValidator;
  private Exam validExam;

  @BeforeEach
  void setUp() {
    examValidator = new ExamValidator();

    // Create a valid exam for base testing
    validExam = new Exam();
    validExam.setCoefficientNumerator(1);
    validExam.setCoefficientDenominator(1);
    validExam.setTitle("Math Exam");
    validExam.setCourseAssignment(new CourseAssignment()); // Assuming this exists
    validExam.setExaminationDate(now());
  }

  @Test
  void acceptSingleExam_WithValidExam_ShouldNotThrowException() {
    assertDoesNotThrow(() -> examValidator.accept(validExam));
  }

  @Test
  void acceptListOfExams_WithValidExams_ShouldNotThrowException() {
    List<Exam> exams = List.of(validExam, validExam);

    assertDoesNotThrow(() -> examValidator.accept(exams));
  }

  @Test
  void acceptSingleExam_WithNullTitle_ShouldThrowBadRequestException() {
    Exam exam = createCopy(validExam);
    exam.setTitle(null);

    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> examValidator.accept(exam));

    assertTrue(exception.getMessage().contains("Title is mandatory"));
  }

  @Test
  void acceptSingleExam_WithNullCourseAssignment_ShouldThrowBadRequestException() {
    Exam exam = createCopy(validExam);
    exam.setCourseAssignment(null);

    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> examValidator.accept(exam));

    assertTrue(exception.getMessage().contains("Awarded course is mandatory"));
  }

  @Test
  void acceptSingleExam_WithNullExaminationDate_ShouldThrowBadRequestException() {
    Exam exam = createCopy(validExam);
    exam.setExaminationDate(null);

    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> examValidator.accept(exam));

    assertTrue(exception.getMessage().contains("Examination date is mandatory"));
  }

  @Test
  void acceptSingleExam_WithZeroCoefficientNumerator_ShouldThrowBadRequestException() {
    Exam exam = createCopy(validExam);
    exam.setCoefficientNumerator(0);

    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> examValidator.accept(exam));

    assertTrue(exception.getMessage().contains("Coefficient numerator can't be less than 0"));
  }

  @Test
  void acceptSingleExam_WithNegativeCoefficientNumerator_ShouldThrowBadRequestException() {
    Exam exam = createCopy(validExam);
    exam.setCoefficientNumerator(-5);

    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> examValidator.accept(exam));

    assertTrue(exception.getMessage().contains("Coefficient numerator can't be less than 0"));
  }

  @Test
  void acceptSingleExam_WithZeroCoefficientDenominator_ShouldThrowBadRequestException() {
    Exam exam = createCopy(validExam);
    exam.setCoefficientDenominator(0);

    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> examValidator.accept(exam));

    assertTrue(exception.getMessage().contains("Coefficient denominator can't be less than 0"));
  }

  @Test
  void acceptSingleExam_WithNegativeCoefficientDenominator_ShouldThrowBadRequestException() {
    Exam exam = createCopy(validExam);
    exam.setCoefficientDenominator(-3);

    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> examValidator.accept(exam));

    assertTrue(exception.getMessage().contains("Coefficient denominator can't be less than 0"));
  }

  @Test
  void acceptSingleExam_WithMultipleViolations_ShouldThrowExceptionWithAllMessages() {
    Exam exam = new Exam();

    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> examValidator.accept(exam));

    String message = exception.getMessage();
    assertTrue(message.contains("Title is mandatory"));
    assertTrue(message.contains("Awarded course is mandatory"));
    assertTrue(message.contains("Examination date is mandatory"));
    assertTrue(message.contains("Coefficient numerator or denominator cannot be null"));
  }

  @Test
  void acceptListOfExams_WithOneInvalidExam_ShouldThrowExceptionOnFirstInvalid() {
    Exam invalidExam = createCopy(validExam);
    invalidExam.setTitle(null);

    List<Exam> exams = List.of(validExam, invalidExam);

    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> examValidator.accept(exams));

    assertTrue(exception.getMessage().contains("Title is mandatory"));
  }

  @Test
  void acceptListOfExams_WithEmptyList_ShouldNotThrowException() {
    List<Exam> emptyList = new ArrayList<>();

    assertDoesNotThrow(() -> examValidator.accept(emptyList));
  }

  @Test
  void acceptListOfExams_WithAllInvalidExams_ShouldThrowExceptionOnFirstExam() {
    Exam invalidExam1 = createCopy(validExam);
    invalidExam1.setTitle(null);

    Exam invalidExam2 = createCopy(validExam);
    invalidExam2.setCourseAssignment(null);

    List<Exam> exams = List.of(invalidExam1, invalidExam2);

    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> examValidator.accept(exams));

    assertTrue(exception.getMessage().contains("Title is mandatory"));
    assertFalse(exception.getMessage().contains("Awarded course is mandatory"));
  }

  private Exam createCopy(Exam original) {
    Exam copy = new Exam();
    copy.setCoefficientNumerator(original.getCoefficientNumerator());
    copy.setCoefficientDenominator(original.getCoefficientDenominator());
    copy.setTitle(original.getTitle());
    copy.setCourseAssignment(original.getCourseAssignment());
    copy.setExaminationDate(original.getExaminationDate());
    return copy;
  }
}
