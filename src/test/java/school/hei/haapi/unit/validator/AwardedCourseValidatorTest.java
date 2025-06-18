package school.hei.haapi.unit.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.haapi.model.AwardedCourse;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.validator.AwardedCourseValidator;

class AwardedCourseValidatorTest {
  AwardedCourseValidator awardedCourseValidator;

  @BeforeEach
  void setUp() {
    awardedCourseValidator = new AwardedCourseValidator();
  }

  @Test
  void awarded_courses_with_bad_data_ko() {
    var awardedCoursesWithoutTeacher = List.of(someAwardedCourse(null, new Course(), new Group()));
    var awardedCoursesWithoutCourse = List.of(someAwardedCourse(new User(), null, new Group()));
    var awardedCoursesWithoutGroup = List.of(someAwardedCourse(new User(), new Course(), null));

    var teacherBadRequestException =
        assertThrows(
            BadRequestException.class,
            () -> awardedCourseValidator.accept(awardedCoursesWithoutTeacher));
    var courseBadRequestException =
        assertThrows(
            BadRequestException.class,
            () -> awardedCourseValidator.accept(awardedCoursesWithoutCourse));
    var groupBadRequestException =
        assertThrows(
            BadRequestException.class,
            () -> awardedCourseValidator.accept(awardedCoursesWithoutGroup));

    assertEquals("Teacher is mandatory", teacherBadRequestException.getMessage());
    assertEquals("Course is mandatory", courseBadRequestException.getMessage());
    assertEquals("Group is mandatory", groupBadRequestException.getMessage());
  }

  private static AwardedCourse someAwardedCourse(User mainTeacher, Course course, Group group) {
    return new AwardedCourse("", mainTeacher, course, group, List.of(), Instant.now());
  }
}
