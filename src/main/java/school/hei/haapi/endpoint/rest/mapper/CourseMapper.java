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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    public List<Course> toRestCourse(List<school.hei.haapi.model.StudentCourse> toCreate){
        return toCreate
                .stream()
                .map(studentCourse -> toRestCourse(studentCourse))
                .collect(toUnmodifiableList());
    }

    public school.hei.haapi.model.Course toDomain(school.hei.haapi.endpoint.rest.model.CrupdateCourse createCourse){
        User teacher = userService.getById(createCourse.getMainTeacherId());
        if (teacher == null) {
            throw new NotFoundException("Teacher.id=" + createCourse.getMainTeacherId() + " is not found");
        }
        if (!teacher.getRole().equals(User.Role.TEACHER)) {
            throw new BadRequestException("Only teachers can possess courses");
        }
        return school.hei.haapi.model.Course.builder()
                .id(createCourse.getId())
                .code(createCourse.getCode())
                .name(createCourse.getName())
                .credits(createCourse.getCredits())
                .totalHours(createCourse.getTotalHours())
                .MainTeacher(User.builder()
                        .id(createCourse.getMainTeacherId())
                        .build())
                .build();
    }
    private school.hei.haapi.model.StudentCourse toDomainStudentCourse(User student, UpdateStudentCourse course){
        school.hei.haapi.model.Course toLink = courseService.getById(course.getCourseId());
        if (!student.getRole().equals(User.Role.STUDENT)) {
            throw new BadRequestException("Only students can be linked to courses");
        }

        if (toLink == null) throw new NotFoundException("Course.id=" + course.getCourseId() + " is not found");
        if (!Objects.equals(course.getStatus(), CourseStatus.LINKED) && !Objects.equals(course.getStatus(), CourseStatus.UNLINKED))
            throw new NotFoundException("Course.id=" + course.getCourseId() + " have wrong status");
        List<StudentCourse> exists = courseService.getCoursesByStudentIdAndCourseId(student.getId(),toLink.getId());
        if (exists.size()>1) throw new NotFoundException("student.id=" + student.getId() + " is in relation with course.id=" + toLink.getId() + "more than one time");
        if (exists.size()==0){
            return school.hei.haapi.model.StudentCourse.builder()
                    .course(toLink)
                    .student(student)
                    .status(StudentCourse.CourseStatus.valueOf(course.getStatus().toString()))
                    .build();
        }
        exists.get(0).setStatus(StudentCourse.CourseStatus.valueOf(course.getStatus().toString()));
        return exists.get(0);
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
