package school.hei.haapi.endpoint.rest.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.StudentCourseExam;
import school.hei.haapi.endpoint.rest.model.StudentExamGrade;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.StudentCourse;
import school.hei.haapi.model.User;
import school.hei.haapi.service.CourseService;
import school.hei.haapi.service.GradeService;
import school.hei.haapi.service.UserService;

@AllArgsConstructor
@Component
public class StudentCourseMapper {
  private final UserService userService;
  private final UserMapper userMapper;
  private final GradeMapper gradeMapper;
  private final GradeService gradeService;
  private final CourseService courseService;

  public StudentCourseExam toRestStudentCourseExam(StudentCourse studentCourse) {
    Course course = courseService.getById(studentCourse.getCourse().getId());
    List<Grade> grades = gradeService.getGradesByStudentCourse(studentCourse);
    List<StudentExamGrade> studentExamGrades =
        grades.stream().map(gradeMapper::toRestStudentExamGrade).collect(Collectors.toList());
    return new StudentCourseExam()
        .id(course.getId())
        .exams(studentExamGrades)
        .name(course.getName())
        .code(course.getCode())
        .credits(course.getCredits())
        .totalHours(course.getTotalHours())
        .mainTeacher(userMapper.toRestTeacher(course.getMainTeacher()));
  }

  public List<StudentCourseExam> toRestStudentCourseExams(List<StudentCourse> studentCourses) {
    List<StudentCourseExam> studentCourseExams = new ArrayList<>();
    for (StudentCourse studentCourse : studentCourses) {
      if (studentCourseExams.stream()
          .anyMatch(obj -> obj.getId() == studentCourse.getCourse().getId())) {
        studentCourseExams
            .stream()
            .filter(obj -> obj.getId() == studentCourse.getCourse().getId())
            .findFirst()
            .ifPresent(
                obj -> {
                  List<StudentExamGrade> studentExamGrades = obj.getExams();
                  studentExamGrades.add(
                      studentCourse.getGrades().stream().map(gradeMapper::toRestStudentExamGrade)
                          .collect(Collectors.toList()).get(0));
                  obj.setExams(studentExamGrades);
                }
            );
      } else {
        studentCourseExams.add(toRestStudentCourseExam(studentCourse));
      }
    }
    return studentCourseExams;
  }

  public User toReestUsert(StudentCourse studentCourse) {
    return userService.getById(studentCourse.getStudent().getId());
  }
}
