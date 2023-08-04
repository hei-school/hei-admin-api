package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.StudentExamGrade;
import school.hei.haapi.endpoint.rest.model.StudentGrade;
import school.hei.haapi.model.Exam;
import school.hei.haapi.endpoint.rest.model.Grade;
import school.hei.haapi.model.User;

@Component
@AllArgsConstructor
public class GradeMapper {
  public school.hei.haapi.model.Grade toDomain(Grade grade) {
    return school.hei.haapi.model.Grade.builder()
        .score(grade.getScore().intValue())
        .creationDatetime(grade.getCreatedAt())
        .build();
  }

  public Grade toRest(school.hei.haapi.model.Grade grade) {
    return new Grade()
        .createdAt(grade.getCreationDatetime())
        .score(Double.valueOf(grade.getScore()));
  }

  public StudentGrade toRestStudentGrade(school.hei.haapi.model.Grade grade, User student) {
    return new StudentGrade().id(student.getId()).ref(student.getRef())
        .firstName(student.getFirstName()).lastName(student.getLastName())
        .email(student.getEmail()).grade(toRest(grade));
  }

  public StudentExamGrade toRestStudentExamGrade(school.hei.haapi.model.Grade grade) {
    return new StudentExamGrade()
        .id(grade.getExam().getId())
        .coefficient(grade.getExam().getCoefficient())
        .examinationDate(grade.getExam().getExaminationDate())
        .title(grade.getExam().getTitle())
        .grade(toRest(grade));
  }
  public StudentExamGrade toRestStudentExamGradeFromGrade(school.hei.haapi.model.Grade grade) {
    return new StudentExamGrade()
        .id(grade.getExam().getId())
        .coefficient(grade.getExam().getCoefficient())
        .examinationDate(grade.getExam().getExaminationDate())
        .title(grade.getExam().getTitle())
        .grade(toRest(grade));
  }

}
