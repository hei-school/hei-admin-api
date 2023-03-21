package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.Course;

@Component
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
public class CourseMapper {

    private String id;
    private String code;
    private int credits;
    private int total_hours;
    private UserMapper user;


    public static CourseMapper fromEntity(Course course){
        return CourseMapper.builder().
                id(course.getId()).
                code(course.getCode()).
                credits(course.getCredits()).
                total_hours(course.getTotal_hours()).
                user(UserMapper.fromEntity(course.getId())).
                build();
    }

    public static Course toEntity(CourseMapper course){
        return Course.builder().
                id(course.getId()).
                code(course.getCode()).
                credits(course.getCredits()).
                total_hours(course.getTotal_hours()).
                .build();
    }
}
