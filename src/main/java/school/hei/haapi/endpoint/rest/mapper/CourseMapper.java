package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.model.Course;

@Component
public class CourseMapper {

    public school.hei.haapi.model.Course  toRest(Course course) {
        var restCourse = new school.hei.haapi.model.Course();
        restCourse.setId(course.getId());
        restCourse.setRef(course.getRef());
        restCourse.setName(course.getName());
        restCourse.setCredits(course.getCredits());
        restCourse.setTotal_hours(course.getTotal_hours());
        return restCourse;
    }

    public Course toDomain(school.hei.haapi.model.Course restCourse) {
        return Course.builder()
                .id(restCourse.getId())
                .ref(restCourse.getRef())
                .name(restCourse.getName())
                .credits(restCourse.getCredits())
                .total_hours(restCourse.getTotal_hours())
                .build();
    }
}
