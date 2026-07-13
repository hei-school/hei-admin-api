package school.hei.haapi.model.dto;

import java.util.List;
import school.hei.haapi.model.CourseAssignment;

public record GroupFlowPeriodCourseAssignment(
    GroupFlowPeriod groupFlowPeriod, List<CourseAssignment> courseAssignments) {}
