package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.model.Course;

@Component
public class CourseMapper {
    public school.hei.haapi.endpoint.rest.model.Course toRestCourse(Course course){
        var restCourse = new school.hei.haapi.endpoint.rest.model.Course();
        restCourse.setId(course.getId());
        restCourse.setRef(course.getRef());
        restCourse.setName(course.getName());
        restCourse.setCredits(course.getCredits());
        restCourse.setTotalHours(course.getTotalHours());
        return restCourse;
    }

    public Course toDomain(school.hei.haapi.endpoint.rest.model.Course restCourse){
        return Course.builder()
                .id(restCourse.getId())
                .ref(restCourse.getRef())
                .name(restCourse.getName())
                .credits(restCourse.getCredits())
                .totalHours(restCourse.getTotalHours())
                .build();
    }
}
