package school.hei.haapi.unit.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.validator.CourseAssignmentValidator;

class CourseAssignmentValidatorTest {
  CourseAssignmentValidator courseAssignmentValidator;

  @BeforeEach
  void setUp() {
    courseAssignmentValidator = new CourseAssignmentValidator();
  }

  @Test
  void awarded_courses_with_bad_data_ko() {
    var courseAssignmentsWithoutTeacher =
        List.of(someCourseAssignment(null, new Course(), List.of(new Group())));
    var courseAssignmentsWithoutCourse =
        List.of(someCourseAssignment(new User(), null, List.of(new Group())));
    var courseAssignmentsWithoutGroup =
        List.of(someCourseAssignment(new User(), new Course(), null));

    var teacherBadRequestException =
        assertThrows(
            BadRequestException.class,
            () -> courseAssignmentValidator.accept(courseAssignmentsWithoutTeacher));
    var courseBadRequestException =
        assertThrows(
            BadRequestException.class,
            () -> courseAssignmentValidator.accept(courseAssignmentsWithoutCourse));
    var groupBadRequestException =
        assertThrows(
            BadRequestException.class,
            () -> courseAssignmentValidator.accept(courseAssignmentsWithoutGroup));

    assertEquals("Teacher is mandatory", teacherBadRequestException.getMessage());
    assertEquals("Course is mandatory", courseBadRequestException.getMessage());
    assertEquals("Group is mandatory", groupBadRequestException.getMessage());
  }

  private static CourseAssignment someCourseAssignment(
      User mainTeacher, Course course, List<Group> groups) {
    return CourseAssignment.builder()
        .id("")
        .mainTeacher(mainTeacher)
        .course(course)
        .groups(groups)
        .exams(List.of())
        .creationDatetime(Instant.now())
        .build();
  }
}
