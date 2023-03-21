package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.User;
import school.hei.haapi.service.UserService;

import java.util.List;

@Component
@AllArgsConstructor
public class CourseMapper {
    private UserService userService;
    public Course toRestCourse(school.hei.haapi.model.Course course, String studentId) {
        return new Course()
                .id(course.getId())
                .code(course.getCode())
                .name(course.getName())
                .credits(course.getCredits())
                .totals_hours(course.getTotalHours())
                .main_teacher(course.getMainTeacher())
                .status(restCourse.getStatus())
                .students(List.of(userService.getById(studentId)));
    }
}
