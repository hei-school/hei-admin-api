package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.model.Courses;
import school.hei.haapi.repository.CoursesRepository;

import java.util.List;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class CourseMapper {
    private final UserMapper userMapper;
    private final CoursesRepository coursesRepository;
    public Courses toDomain(school.hei.haapi.endpoint.rest.model.Course entity){
        return Courses.builder()
                .id(entity.getId())
                .name(entity.getName())
                .code(entity.getCode())
                .credits(entity.getCredits())
                .total_hours(entity.getTotalHours())
                .main_teacher(userMapper.toDomain(entity.getMainTeacher()))
                .build();
    }
    public school.hei.haapi.endpoint.rest.model.Course toRest(Courses domain){
        var restCourse = new school.hei.haapi.endpoint.rest.model.Course();
        restCourse.setId(domain.getId());
        restCourse.setName(domain.getName());
        restCourse.setCode(domain.getCode());
        restCourse.setCredits(domain.getCredits());
        restCourse.setTotalHours(domain.getTotal_hours());
        restCourse.setMainTeacher(userMapper.toRestTeacher(domain.getMain_teacher()));
        return restCourse;
    }
}
