package school.hei.haapi.model.dto;

import java.util.List;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseAssignment;

public record CourseDto(Course course, List<CourseAssignment> courseAssigments) {}
