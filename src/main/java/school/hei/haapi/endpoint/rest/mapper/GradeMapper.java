package school.hei.haapi.endpoint.rest.mapper;

import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.GetStudentGrade;
import school.hei.haapi.endpoint.rest.model.Grade;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.User;

@Component
@AllArgsConstructor
public class GradeMapper {
  private final UserMapper userMapper;

  // todo: to review all class
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

  public GetStudentGrade toRestStudentGrade(school.hei.haapi.model.Grade grade) {
    if (grade == null) {
      return null;
    }
    var getStudentGrade =  new GetStudentGrade().grade(toRest(grade));
    getStudentGrade.setStudent(userMapper.toRestStudent(grade.getStudent()));

    return getStudentGrade;
  }

  public GetStudentGrade toRestStudentExamGrade(User student, Exam exam) {
    Optional<school.hei.haapi.model.Grade> optionalGrade =
        exam.getGrades().stream()
            .filter(grade -> grade.getStudent().getId().equals(student.getId()))
            .findFirst();
    school.hei.haapi.model.Grade grade = optionalGrade.get();
    return new GetStudentGrade().grade(toRest(grade));
  }

  //  public ExamDetail toRestExamDetail(Exam exam, List<school.hei.haapi.model.Grade> grades) {
  //    return new ExamDetail()
  //        .id(exam.getId())
  //        .coefficient(exam.getCoefficient())
  //        .title(exam.getTitle())
  //        .examinationDate(exam.getExaminationDate().atZone(ZoneId.systemDefault()).toInstant())
  //        .participants(
  //            grades.stream().map(grade -> this.toRestStudentGrade(grade)).collect(toList()));
  //  }
}
