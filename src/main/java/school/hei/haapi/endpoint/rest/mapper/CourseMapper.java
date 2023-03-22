package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.response.CoursesResponse;
import school.hei.haapi.endpoint.rest.response.CreateCourses;
import school.hei.haapi.endpoint.rest.response.StudentCoursesResponse;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.StudentCourses;
import school.hei.haapi.repository.UserRepository;

@Component
@AllArgsConstructor
public class CourseMapper {
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    public Course toDomain(CoursesResponse domain){
        return Course.builder()
                .id(domain.getId())
                .code(domain.getCode())
                .name(domain.getName())
                .credits(domain.getCredits())
                .total_hours(domain.getTotal_hours())
                .main_teacher(userMapper.toDomain(domain.getMain_teacher()))
                .build();
    }

    public Course toDomain(CreateCourses domain){
        return Course.builder()
                .id(domain.getId())
                .code(domain.getCode())
                .name(domain.getName())
                .credits(domain.getCredits())
                .total_hours(domain.getTotal_hours())
                .main_teacher(userRepository.findById(domain.getMain_teacher_id()).get())
                .build();
    }

    public CoursesResponse responseToRest(Course rest){
        return CoursesResponse.builder()
                .id(rest.getId())
                .code(rest.getCode())
                .name(rest.getName())
                .credits(rest.getCredits())
                .total_hours(rest.getTotal_hours())
                .main_teacher(userMapper.toRestTeacher(rest.getMain_teacher()))
                .build();
    }

    public CreateCourses createToRest(Course rest){
        return CreateCourses.builder()
                .id(rest.getId())
                .code(rest.getCode())
                .name(rest.getName())
                .credits(rest.getCredits())
                .total_hours(rest.getTotal_hours())
                .main_teacher_id(rest.getMain_teacher().getId())
                .build();
    }

    public StudentCoursesResponse toRest(StudentCourses rest) {
        return StudentCoursesResponse.builder()
                .course(rest.getCourse())
                .build();
    }
}
