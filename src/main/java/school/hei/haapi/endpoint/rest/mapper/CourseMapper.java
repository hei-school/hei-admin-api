package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.Course;

@Component
@AllArgsConstructor
public class CourseMapper {
    public Course toDomain (Course course){
        return Course.builder()
                .id(course.getId())
                .name(course.getName())
                .credits(course.getCredits())
                .ref(course.getRef())
                .total_hours(course.getTotal_hours())
                .build();
    }
    public Course toRest (Course course){
        Course nCourse = new Course();
        nCourse.setId(course.getId());
        nCourse.setRef(course.getRef());
        nCourse.setName(course.getName());
        nCourse.setCredits(course.getCredits());
        nCourse.setTotal_hours(course.getTotal_hours());

        return nCourse;
    }
}
