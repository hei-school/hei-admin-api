package school.hei.haapi.integration.utils;

import school.hei.haapi.endpoint.rest.model.CrupdateCourseAssignment;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.User;

import java.util.List;
import java.util.UUID;

public class CourseAssignmentUtils {
    public static CourseAssignment createCourseAssignment(
            Course course, User mainTeacher, List<Group> groups) {
        return CourseAssignment.builder()
                .id(UUID.randomUUID().toString())
                .course(course)
                .mainTeacher(mainTeacher)
                .groups(groups)
                .build();
    }

    public static CrupdateCourseAssignment createCrupdateCourseAssignment(
            Course course, User mainTeacher, List<Group> groups) {
        return new CrupdateCourseAssignment()
                .id(UUID.randomUUID().toString())
                .courseId(course.getId())
                .mainTeacherId(mainTeacher.getId())
                .groupIds(groups.stream().map(Group::getId).toList());
    }
}
