package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.model.Course;

public class CourseMapper {
    @Component
    public Course toRestCourse(Course course) {
        Course restCourse = new Course();
        restCourse.getId();

        restCourse.setCode(course.getCode());
        restCourse.setName(course.getName());
        restCourse.setCredits(course.getCredits());
        restCourse.setTotal_hours(course.getTotal_hours());
        restCourse.setMain_teacher(course.getMain_teacher());

        return restCourse;
    }
    public Course toDomain(Course course) {
        return Course.builder()
                .id(course.getId())
                .code(course.getCode())
                .name(course.getName())
                .credits(course.getCredits())
                .total_hours(course.getTotal_hours())
                .main_teacher(Course.Status.valueOf(course.getStatus().toString()));
    }
}
