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

@AllArgsConstructor
@Component
public class StudentCourseMapper {
  private final UserMapper userMapper;
  private final GradeMapper gradeMapper;
  private final ExamMapper examMapper;

  public StudentCourseExam toRestStudentCourseExam(Course course,
                                                   List<StudentExamGrade> studentExamGrades) {
    return new StudentCourseExam()
        .id(course.getId())
        .exams(studentExamGrades)
        .name(course.getName())
        .code(course.getCode())
        .credits(course.getCredits())
        .totalHours(course.getTotalHours());
//        .mainTeacher(userMapper.toRestTeacher(course.getMainTeacher()));
  }

  public List<StudentCourseExam> toRestStudentCourseExams(List<StudentCourse> studentCourses) {
    List<StudentCourseExam> studentCourseExams = new ArrayList<>();
    for (StudentCourse studentCourse : studentCourses) {
      Course course = studentCourse.getCourse();
      //check whether the course already exists via its ID in this List<StudentCourseExam>
      if (studentCourseExams.stream()
          .anyMatch(studentCourseExam -> studentCourseExam.getId() ==
              studentCourse.getCourse().getId())) {
        //if the course exists, add an exam to it
        studentCourseExams.stream()
            //filtered on the courses in question, adding the exam only to their courses
            .filter(
                studentCourseExam -> studentCourseExam.getId() == studentCourse.getCourse().getId())
            .forEach(
                studentCourseExam -> {
                  //take all grades from one student in one course (studentCourse)
                  List<Grade> grades = studentCourse.getGrades();
                  //transform grades to StudentExamGrade
                  List<StudentExamGrade> newStudentExamGrades = grades.stream()
                      .map(grade -> gradeMapper.toRestStudentExamGrade(grade))
                      .collect(Collectors.toList());
                  //take list of the studentExamGrades (*grade) of this studentCourseExam (*course and exam)
                  List<StudentExamGrade> studentExamGrades = studentCourseExam.getExams();
                  studentExamGrades.addAll(newStudentExamGrades);
                  //add new list of studentExamGrades without duplicate
                  studentCourseExam.setExams(studentExamGrades.stream().distinct().collect(
                      Collectors.toList()));
                }
            );
      } else {
        //if the course does not exist, create new StudentCourseExam
        List<StudentExamGrade> studentExamGrades = course.getExams().stream()
            .map(exam -> examMapper.toRestExamExamGrade(exam, studentCourse))
            .collect(Collectors.toList());
        studentCourseExams.add(toRestStudentCourseExam(course, studentExamGrades));
      }
    }
    return studentCourseExams;
  }
}
