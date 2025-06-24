package school.hei.haapi.unit.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.haapi.model.StudentAttendance;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.validator.AttendanceValidator;

class AttendanceValidatorTest {
  AttendanceValidator subject;

  @BeforeEach
  void setUp() {
    subject = new AttendanceValidator();
  }

  @Test
  void attendance_without_student_ko() {
    var studentAttendance = List.of(new StudentAttendance());

    var badRequestException =
        assertThrows(BadRequestException.class, () -> subject.accept(studentAttendance));
    assertEquals("Student is mandatory", badRequestException.getMessage());
  }
}
