package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.Teacher;
import school.hei.haapi.endpoint.rest.model.User;

@Component
@AllArgsConstructor
public class CourseMapper {
    private final UserMapper userMapper;
    public Course toRest(school.hei.haapi.model.Course course){
        Teacher mainTeacher = userMapper
                .toRestTeacher(course.getMainTeacher());
        Course restCourse = new Course();
        restCourse.setId(course.getId());
        restCourse.setCode(course.getCode());
        restCourse.setName(course.getName());
        restCourse.setCredits(course.getCredits());
        restCourse.setTotalHours(course.getTotalHours());
        restCourse.setMainTeacher(mainTeacher);
        return restCourse;
    }

    public Course toEntity(Course course, User mainTeacher, User student) {
        return Course.builder()
                .id(course.getId())
                .code(course.getCode())
                .name(course.getName())
                .credits(course.getCredits())
                .totalHours(course.getTotalHours())
                .mainTeacher(mainTeacher)
                .student(student)
                .status(course.getStatus())
                .build();
    }
}
