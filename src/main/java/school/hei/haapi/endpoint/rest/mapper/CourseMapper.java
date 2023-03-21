package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.model.User;

@Component
@AllArgsConstructor
public class CourseMapper {

    private final UserMapper userMapper;

    public Course toRest(school.hei.haapi.model.Course course) {
        Course rest = new Course();
        rest.setId(course.getId());
        rest.setCode(course.getCode());
        rest.setName(course.getName());
        rest.setCredits(course.getCredits());
        rest.totalHours(course.getTotal_hours());
        rest.setMainTeacher(userMapper.toRestTeacher(course.getMain_teacher()));
        return rest;
    }

    public school.hei.haapi.model.Course toDomain(Course course){
        return school.hei.haapi.model.Course.builder()
                .id(course.getId())
                .code(course.getCode())
                .name(course.getName())
                .credits(course.getCredits())
                .total_hours(course.getTotalHours())
                .main_teacher(userMapper.toDomain(course.getMainTeacher()))
                .build();
    }
}

