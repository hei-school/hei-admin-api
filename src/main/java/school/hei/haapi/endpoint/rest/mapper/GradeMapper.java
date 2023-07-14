package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.StudentExamGrade;
import school.hei.haapi.endpoint.rest.model.StudentGrade;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.User;
import school.hei.haapi.service.ExamService;
import school.hei.haapi.service.UserService;

@Component
@AllArgsConstructor
public class GradeMapper {
  private final UserService userService;
  private final ExamService examService;


  public Grade toDomain(school.hei.haapi.endpoint.rest.model.Grade grade) {
    return Grade.builder()
        .score(grade.getScore().intValue())
        .creationDatetime(grade.getCreatedAt())
        .build();
  }

  public school.hei.haapi.endpoint.rest.model.Grade toRest(Grade grade) {
    return new school.hei.haapi.endpoint.rest.model.Grade()
        .createdAt(grade.getCreationDatetime())
        .score(Double.valueOf(grade.getScore()));
  }

  public school.hei.haapi.endpoint.rest.model.StudentGrade toRestStudentGrade(Grade grade) {
    StudentGrade studentGrade = new StudentGrade();
    User student = userService.getById(grade.getStudentCourse().getStudent().getId());

    studentGrade.setId(student.getId());
    studentGrade.setRef(student.getRef());
    studentGrade.setFirstName(student.getFirstName());
    studentGrade.setLastName(student.getLastName());
    studentGrade.setEmail(student.getEmail());
    school.hei.haapi.endpoint.rest.model.Grade restGrade = toRest(grade);
    studentGrade.setGrade(restGrade);

    return studentGrade;
  }

  public StudentExamGrade toRestStudentExamGrade(Grade grade) {
    Exam exam = examService.getExamById(grade.getExam().getId());
    StudentExamGrade studentGrade = new StudentExamGrade()
        .id(exam.getId())
        .coefficient(exam.getCoefficient())
        .examinationDate(exam.getExaminationDate())
        .title(exam.getTitle())
        .grade(toRest(grade));
    return studentGrade;
  }
}
