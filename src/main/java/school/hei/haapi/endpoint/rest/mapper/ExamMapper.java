package school.hei.haapi.endpoint.rest.mapper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.ExamDetail;
import school.hei.haapi.endpoint.rest.model.ExamInfo;
import school.hei.haapi.endpoint.rest.model.StudentCourseExam;
import school.hei.haapi.endpoint.rest.model.StudentExamGrade;
import school.hei.haapi.endpoint.rest.model.StudentGrade;
import school.hei.haapi.endpoint.rest.model.Teacher;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.StudentCourse;
import school.hei.haapi.model.User;
import school.hei.haapi.service.CourseService;
import school.hei.haapi.service.GradeService;
import school.hei.haapi.service.UserService;

@Component
@AllArgsConstructor
public class ExamMapper {
  private final CourseService courseService;
  private final UserMapper userMapper;
  private final GradeService gradeService;
  private final GradeMapper gradeMapper;
  private final UserService userService;

  public Exam toDomain(StudentCourseExam studentCourseExam) {
    Course course = courseService.getById(studentCourseExam.getId());
    List<StudentExamGrade> exams = studentCourseExam.getExams();
    // Vérifier que exams n'est pas vide
    StudentExamGrade studentExamGrade = exams.get(0);


    return Exam.builder().id(studentCourseExam.getId()).course(course)
        .coefficient(studentExamGrade.getCoefficient()).title(studentExamGrade.getTitle())
        .examinationDate(
            LocalDateTime.ofInstant(studentExamGrade.getExaminationDate(), ZoneId.systemDefault()))
        .build();

  }

  public StudentCourseExam toRest(Exam exam) {
    StudentCourseExam studentCourseExam = new StudentCourseExam();

    studentCourseExam.setId(exam.getId());

    Course course = exam.getCourse();
    studentCourseExam.setCode(course.getCode());
    studentCourseExam.setName(course.getName());
    studentCourseExam.setCredits(course.getCredits());
    studentCourseExam.setTotalHours(course.getTotalHours());

    User teacher = course.getMainTeacher();
    Teacher mainTeacher = userMapper.toRestTeacher(teacher);
    //studentCourseExam.setMainTeacher(mainTeacher);

    List<StudentExamGrade> exams = new ArrayList<>();
    StudentExamGrade studentExamGrade = new StudentExamGrade();
    studentExamGrade.setId(exam.getId());
    studentExamGrade.setCoefficient(exam.getCoefficient());
    studentExamGrade.setTitle(exam.getTitle());
    studentExamGrade.setExaminationDate(
        exam.getExaminationDate().atZone(ZoneId.systemDefault()).toInstant());
    exams.add(studentExamGrade);

    studentCourseExam.setExams(exams);

    return studentCourseExam;
  }

  public ExamInfo toExamInfo(Exam exam) {
    ExamInfo examInfo = new ExamInfo();
    examInfo.setId(exam.getId());
    examInfo.setCoefficient(exam.getCoefficient());
    examInfo.setTitle(exam.getCourse().getName());
    examInfo.setExaminationDate(
        exam.getExaminationDate().atZone(ZoneId.systemDefault()).toInstant());

    return examInfo;
  }

  public ExamDetail toExamDetail(Exam exam) {
    ExamDetail examDetail = new ExamDetail();
    examDetail.setId(exam.getId());
    examDetail.setCoefficient(exam.getCoefficient());
    examDetail.setTitle(exam.getTitle());
    examDetail.setExaminationDate(
        exam.getExaminationDate().atZone(ZoneId.systemDefault()).toInstant());

    List<StudentGrade> participants = new ArrayList<>();
    Grade grade = gradeService.getGradeByExamId(exam.getId());
   for (StudentCourse student : exam.getCourse().getStudentCourses()) {
     StudentGrade studentGrade = new StudentGrade();
     User user = userService.getById(student.getUserId().getId());
     studentGrade.setId(user.getId());
     studentGrade.setRef(user.getRef());
     studentGrade.setFirstName(user.getFirstName());
     studentGrade.setLastName(user.getLastName());
     studentGrade.setEmail(user.getEmail());
     studentGrade.setGrade(gradeMapper.toRestGrade(grade));

     participants.add(studentGrade);
   }

    examDetail.setParticipants(participants);

    return examDetail;
  }


}
