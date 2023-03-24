package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Course;

@Component
@AllArgsConstructor
public class CourseMapper {
    private final UserMapper userMapper;
    public Course toRest(school.hei.haapi.model.Course domain){
        return new Course()
                .id(domain.getId())
                .code(domain.getCode())
                .name(domain.getName())
                .credits(domain.getCredits())
                .totalHours(domain.getTotalHours())
                .mainTeacher(userMapper.toRestTeacher(domain.getMainTeacher()));
    }
}
