package school.hei.haapi.endpoint.rest.mapper;

import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.CrupdateCourse;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.UserRepository;

@Component
@AllArgsConstructor
public class CourseMapper {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CourseRepository courseRepository;

    public Course toRest(school.hei.haapi.model.Course domain) {
        return new Course()
                .id(domain.getId())
                .code(domain.getCode())
                .name(domain.getName())
                .credits(domain.getCredits())
                .totalHours(domain.getTotalHours())
                .mainTeacher(userMapper.toRestTeacher(domain.getMainTeacher()));
    }

    public school.hei.haapi.model.Course toDomainCourse(CrupdateCourse rest) {
        Optional<school.hei.haapi.model.Course> optionalCourse =
                courseRepository.findById(rest.getId());
        Optional<User> teacher = userRepository.findById(rest.getMainTeacherId());
        if (optionalCourse.isPresent()) {
            school.hei.haapi.model.Course persisted = optionalCourse.get();
            persisted.setCode(rest.getCode());
            persisted.setName(rest.getName());
            persisted.setCredits(rest.getCredits());
            persisted.setTotalHours(rest.getTotalHours());
            persisted.setMainTeacher(teacher.get());
            return persisted;
        }
        return school.hei.haapi.model.Course.builder()
                .id(rest.getId())
                .code(rest.getCode())
                .name(rest.getName())
                .credits(rest.getCredits())
                .totalHours(rest.getTotalHours())
                .mainTeacher(teacher.get())
                .build();
    }

}
