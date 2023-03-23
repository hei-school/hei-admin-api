package school.hei.haapi.unit.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.model.UpdateStudentCourse;
import school.hei.haapi.endpoint.rest.validator.UpdateStudentCourseValidator;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static school.hei.haapi.endpoint.rest.model.CourseStatus.UNLINKED;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsBadRequestException;


class UpdateStudentCourseValidatorTest {
  UpdateStudentCourseValidator subject;

  @BeforeEach
  void setUp() {
    subject = new UpdateStudentCourseValidator();
  }

  @Test
  void validator_validate_student_course_ok() {
    UpdateStudentCourse updateStudentCourse = new UpdateStudentCourse()
        .courseId(String.valueOf(randomUUID()))
        .status(UNLINKED);

    assertDoesNotThrow(() -> subject.accept(updateStudentCourse));
  }

  @Test
  void validator_validate_student_course_ko() {
    UpdateStudentCourse invalidUpdateStudentCourse = new UpdateStudentCourse()
        .courseId(null)
        .status(null);

    assertThrowsBadRequestException("CourseId is mandatory. Status is mandatory.",
        () -> subject.accept(invalidUpdateStudentCourse));
  }
}
