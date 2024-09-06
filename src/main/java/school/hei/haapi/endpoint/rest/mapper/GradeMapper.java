package school.hei.haapi.endpoint.rest.mapper;

import static java.util.stream.Collectors.toList;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateGrade;
import school.hei.haapi.endpoint.rest.model.ExamDetail;
import school.hei.haapi.endpoint.rest.model.Grade;
import school.hei.haapi.endpoint.rest.model.StudentExamGrade;
import school.hei.haapi.endpoint.rest.model.StudentGrade;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.User;
import school.hei.haapi.service.ExamService;
import school.hei.haapi.service.UserService;

@Component
@AllArgsConstructor
public class GradeMapper {
  private final UserService userService;
  private final ExamService examService;

  public school.hei.haapi.model.Grade toDomain(CreateGrade restGrade) {
    return school.hei.haapi.model.Grade.builder()
            .student(userService.findById(restGrade.getStudentId()))
            .exam(examService.findById(restGrade.getExamId()))
            .score(restGrade.getScore())
            .creationDatetime(Instant.now())
            .build();
>>>>>>> e3c540d (feat: enable floating score number when inserting grade)
  }

  public school.hei.haapi.model.Grade toDomain(Grade grade) {
    return school.hei.haapi.model.Grade.builder()
        .score(grade.getScore())
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
    return new StudentGrade()
        .id(grade.getStudent().getId())
        .ref(grade.getStudent().getRef())
        .firstName(grade.getStudent().getFirstName())
        .lastName(grade.getStudent().getLastName())
        .email(grade.getStudent().getEmail())
        .grade(toRest(grade));
  }

  public StudentExamGrade toRestStudentExamGrade(User student, Exam exam) {
    Optional<school.hei.haapi.model.Grade> optionalGrade =
        exam.getGrades().stream()
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
    return new ExamDetail()
        .id(exam.getId())
        .coefficient(exam.getCoefficient())
        .title(exam.getTitle())
        .examinationDate(exam.getExaminationDate().atZone(ZoneId.systemDefault()).toInstant())
        .participants(
            grades.stream().map(grade -> this.toRestStudentGrade(grade)).collect(toList()));
  }
}
