package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.Teacher;
import school.hei.haapi.endpoint.rest.model.CrupdateCourse;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.service.UserService;

@Component
@AllArgsConstructor
public class CourseMapper {
    private UserService userService;
    private UserMapper userMapper;

    public Course toRestCourse(school.hei.haapi.model.Course course) {
        return new Course()
                .id(course.getId())
                .code(course.getCode())
                .name(course.getName())
                .credits(course.getCredits())
                .totalHours(course.getTotalHours())
                .mainTeacher(course.getMainTeacher() != null ?
                        userMapper.toRestTeacher(course.getMainTeacher()) : null);
    }

    public school.hei.haapi.model.Course toDomainCrupdateCourse(CrupdateCourse crupdateCourse) {
        User teacher = userService.getById(crupdateCourse.getMainTeacherId());
        if (teacher == null) {
            throw new NotFoundException("Teacher.id=" + crupdateCourse.getId() + " is not found");
        }
        return school.hei.haapi.model.Course.builder()
                .id(crupdateCourse.getId())
                .code(crupdateCourse.getCode())
                .name(crupdateCourse.getName())
                .credits(crupdateCourse.getCredits())
                .totalHours(crupdateCourse.getTotalHours())
                .mainTeacher(teacher)
                .build();
    }
}
