package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Teacher;
import school.hei.haapi.model.Course;

@Component
@AllArgsConstructor
public class CourseMapper {
    private UserMapper userMapper;

    public school.hei.haapi.endpoint.rest.model.Course toRest(Course course) {
        Teacher teacher = userMapper.toRestTeacher(course.getTeacher());

        return new school.hei.haapi.endpoint.rest.model.Course()
                .id(course.getId())
                .name(course.getName())
                .code(course.getCode())
                .credits(course.getCredits())
                .mainTeacher(teacher);
    }
}