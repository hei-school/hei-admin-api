package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.Course;
@Component
@AllArgsConstructor
public class CourseMapper {
    public Course toRestCourse(Course course){
        return new Course().builder()
                .build();
    }
}
