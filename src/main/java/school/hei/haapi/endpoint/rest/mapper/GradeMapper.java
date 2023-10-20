package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.ExamDetail;
import school.hei.haapi.endpoint.rest.model.StudentExamGrade;
import school.hei.haapi.endpoint.rest.model.StudentGrade;
import school.hei.haapi.model.Exam;
import school.hei.haapi.endpoint.rest.model.Grade;
import school.hei.haapi.model.User;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class GradeMapper {
  //todo: to review all class
  public school.hei.haapi.model.Grade toDomain(Grade grade) {
    return school.hei.haapi.model.Grade.builder()
        .score(grade.getScore().intValue())
        .creationDatetime(grade.getCreatedAt())
        .build();
  }

  public Grade toRest(school.hei.haapi.model.Grade grade) {
    return new Grade()
        .id(grade.getId())
        .createdAt(grade.getCreationDatetime())
        .score(grade.getScore().doubleValue());
  }

  public StudentGrade toRestStudentGrade(school.hei.haapi.model.Grade grade) {
    if (grade == null) {
      return null;
    }
    return new StudentGrade()
        .id(grade.getStudent().getId())
        .ref(grade.getStudent().getRef())
        .firstName(grade.getStudent().getFirstName())
        .lastName(grade.getStudent().getLastName())
        .email(grade.getStudent().getEmail())
        .grade(toRest(grade));
  }

  public StudentExamGrade toRestStudentExamGrade(User student, Exam exam) {
    Optional<school.hei.haapi.model.Grade> optionalGrade = exam.getGrades().stream()
        .filter(grade -> grade.getStudent().getId().equals(student.getId()))
        .findFirst();
    school.hei.haapi.model.Grade grade = optionalGrade.get();
    return new StudentExamGrade()
        .id(grade.getExam().getId())
        .coefficient(grade.getExam().getCoefficient())
        .examinationDate(grade.getExam().getExaminationDate())
        .title(grade.getExam().getTitle())
        .grade(toRest(grade));
  }

  public ExamDetail toRestExamDetail(Exam exam, List<school.hei.haapi.model.Grade> grades) {
    return new ExamDetail().id(exam.getId()).coefficient(exam.getCoefficient())
        .title(exam.getTitle())
        .examinationDate(exam.getExaminationDate().atZone(ZoneId.systemDefault()).toInstant())
        .participants(grades.stream()
            .map(grade -> this.toRestStudentGrade(grade))
            .collect(Collectors.toList()));
  }
}
