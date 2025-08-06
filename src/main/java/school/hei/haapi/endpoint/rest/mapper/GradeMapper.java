package school.hei.haapi.endpoint.rest.mapper;

import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CrupdateGrade;
import school.hei.haapi.endpoint.rest.model.Grade;
import school.hei.haapi.endpoint.rest.model.StudentGrade;
import school.hei.haapi.endpoint.rest.validator.GradeValidator;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.GradeRepository;
import school.hei.haapi.service.ExamService;
import school.hei.haapi.service.UserService;

@Component
@AllArgsConstructor
public class GradeMapper {
  private final UserMapper userMapper;
  private final ExamService examService;
  private final UserService userService;
  private final GradeValidator validator;
  private final ExamMapper examMapper;
  private final GradeRepository gradeRepository;

  // todo: to review all class
  public school.hei.haapi.model.Grade toDomain(Grade grade) {
    return school.hei.haapi.model.Grade.builder()
        .score(grade.getScore())
        .creationDatetime(grade.getCreatedAt())
        .build();
  }

  public Grade toRest(school.hei.haapi.model.Grade grade) {
    return new Grade()
        .id(grade.getId())
        .exam(examMapper.toRest(grade.getExam()))
        .createdAt(grade.getCreationDatetime())
        .score(grade.getScore());
    // TODO: implement update time or better yet : grade history here !
  }

  public StudentGrade toRestStudentGrade(school.hei.haapi.model.Grade grade) {
    if (grade == null) {
      return null;
    }
    var getStudentGrade = new StudentGrade().grade(toRest(grade));
    getStudentGrade.setStudent(userMapper.toRestStudent(grade.getStudent()));

    return getStudentGrade;
  }

  public StudentGrade toRestStudentExamGrade(User student, Exam exam) {
    Optional<school.hei.haapi.model.Grade> optionalGrade =
        exam.getGrades().stream()
            .filter(grade -> grade.getStudent().getId().equals(student.getId()))
            .findFirst();
    school.hei.haapi.model.Grade grade = optionalGrade.get();
    var getStudentGrade = new StudentGrade().grade(toRest(grade));
    getStudentGrade.setStudent(userMapper.toRestStudent(student));
    return getStudentGrade;
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

  public school.hei.haapi.model.Grade toDomain(
      CrupdateGrade grade, String examId, String studentRef) {
    validator.accept(grade);

    return gradeRepository
        .getGradeByExamIdAndStudentRef(examId, studentRef)
        .orElse(
            new school.hei.haapi.model.Grade(
                examService.getExamById(examId),
                userService.findByRef(studentRef),
                grade.getScore()));
  }
}
