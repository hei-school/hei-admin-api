package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.Course;

@Component
@AllArgsConstructor
public class CourseMapper {
    public  Course toRestCourse(Course course){
        return new Course().toBuilder()
                .id(course.getId())
                .name(course.getName())
                .code(course.getCode())
                .credits(course.getCredits())
                .totalHours(course.getTotalHours())
                .mainTeacher(course.getMainTeacher())
                .build();
    }
}
