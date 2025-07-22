package school.hei.haapi.integration.test_data;

import static java.util.UUID.randomUUID;

import java.util.List;
import school.hei.haapi.endpoint.rest.model.CrupdateCourseAssignment;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.User;

public class CourseAssignmentTestData {
  public static CourseAssignment createCourseAssignment(
      Course course, User mainTeacher, List<Group> groups) {
    return CourseAssignment.builder()
        .id(randomUUID().toString())
        .course(course)
        .mainTeacher(mainTeacher)
        .groups(groups)
        .build();
  }

  public static CrupdateCourseAssignment createCrupdateCourseAssignment(
      Course course, User mainTeacher, List<Group> groups) {
    return new CrupdateCourseAssignment()
        .id(randomUUID().toString())
        .courseId(course.getId())
        .mainTeacherId(mainTeacher.getId())
        .groupIds(groups.stream().map(Group::getId).toList());
  }
}
