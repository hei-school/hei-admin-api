package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.Teacher;

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
}
