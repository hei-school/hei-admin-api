package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.model.Courses;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;

@Component
public class CoursesMapper {
    private UserMapper userMapper;
    public school.hei.haapi.endpoint.rest.model.Course toRest(Courses course) {
        var restCourse = new Course();
        restCourse.setId(course.getId());
        restCourse.setCode(course.getCode());
        restCourse.setName(course.getName());
        restCourse.setCredits(course.getCredits());
        restCourse.setTotalHours(course.getTotalHours());
        restCourse.setMainTeacher(userMapper.toRestTeacher(course.getMainTeacher()));

        return restCourse;
    }

    public Courses toDomain(Course restCourse) {
        return Courses.builder()
                .id(restCourse.getId())
                .code(restCourse.getCode())
                .name(restCourse.getName())
                .credits(restCourse.getCredits())
                .totalHours(restCourse.getTotalHours())
                .mainTeacher(userMapper.toDomain(restCourse.getMainTeacher()))
                .build();
    }
}
