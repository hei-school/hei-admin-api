package school.hei.haapi.endpoint.rest.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import school.hei.haapi.model.StudentCourse;

@AllArgsConstructor
@Data
@Builder
public class UpdateStudentCourseStatusResponse {
    private String course_id;
    private StudentCourse.Status status;
}
