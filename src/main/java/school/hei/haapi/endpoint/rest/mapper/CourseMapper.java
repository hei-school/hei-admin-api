package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.CourseStatus;
import school.hei.haapi.endpoint.rest.model.UpdateStudentCourse;
import school.hei.haapi.model.StudentCourse;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.service.CourseService;
import school.hei.haapi.service.UserService;

import java.util.List;

import static java.util.stream.Collectors.toUnmodifiableList;

@Component
@AllArgsConstructor
public class CourseMapper {
    private final UserMapper userMapper;
    private final UserService userService;

    private final CourseService courseService;

    public Course toRestCourse(school.hei.haapi.model.Course course){
              return new Course()
                      .id(course.getId())
                      .code(course.getCode())
                      .credits(course.getCredits())
                      .name(course.getName())
                      .totalHours(course.getTotalHours())
                      .mainTeacher(userMapper.toRestTeacher(course.getMainTeacher()));
    }

    public Course toRestCourse(school.hei.haapi.model.StudentCourse studentCourse){
        return toRestCourse(studentCourse.getCourse());
    }

    private school.hei.haapi.model.StudentCourse toDomainStudentCourse(User student, UpdateStudentCourse course){
        if (!student.getRole().equals(User.Role.STUDENT)) {
            throw new BadRequestException("Only students can be linked to courses");
        }
        school.hei.haapi.model.Course toSave = courseService.getById(course.getCourseId());
        if (toSave == null) {
            throw new NotFoundException("Course.id=" + course.getCourseId() + " is not found");
        }
        return school.hei.haapi.model.StudentCourse.builder()
                .course(toSave)
                .student(student)
                .status(StudentCourse.CourseStatus.valueOf(course.getStatus().toString()))
                .build();
    }

    public List<school.hei.haapi.model.StudentCourse> toDomainStudentCourse(String studentId, List<UpdateStudentCourse> toCreate){
        User student = userService.getById(studentId);
        if (student == null) {
            throw new NotFoundException("Student.id=" + studentId + " is not found");
        }
        return toCreate
                .stream()
                .map(createStudentCourse -> toDomainStudentCourse(student, createStudentCourse))
                .collect(toUnmodifiableList());
    }
}
