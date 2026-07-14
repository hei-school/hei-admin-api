package school.hei.haapi.model.dto;

import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseAssignment;

import java.util.List;

public record CourseDto(Course course, List<CourseAssignment> courseAssigments) {}
