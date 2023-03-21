package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.Teacher;


@Component
@AllArgsConstructor
public class CourseMapper {

    private  final  UserMapper userMapper;
    public  Course toRestCourse(school.hei.haapi.model.Course course){

        return new  school.hei.haapi.endpoint.rest.model.Course()
                .id(course.getId())
                .name(course.getName())
                .code(course.getCode())
                .credits(course.getCredits())
                .totalHours(course.getTotalHours())
                .mainTeacher(userMapper.toRestTeacher(course.getMainTeacher()) );
    }
}
