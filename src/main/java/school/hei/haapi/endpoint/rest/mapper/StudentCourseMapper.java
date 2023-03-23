package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.controller.response.UpdateStudentCourseStatusResponse;
import school.hei.haapi.endpoint.rest.model.UpdateStudentCourse;
import school.hei.haapi.model.StudentCourse;
import school.hei.haapi.repository.StudentCourseRepository;

@Component
@AllArgsConstructor
public class StudentCourseMapper {

    public UpdateStudentCourseStatusResponse toConvert(UpdateStudentCourse course){
        return UpdateStudentCourseStatusResponse
                .builder()
                .course_id(course.getCourseId())
                .status(StudentCourse.CourseStatus.valueOf(course.getStatus().toString()))
                .build();
    }
}
