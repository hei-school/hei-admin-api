package school.hei.haapi.endpoint.rest.mapper;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CrupdateGrade;
import school.hei.haapi.endpoint.rest.model.Grade;
import school.hei.haapi.endpoint.rest.model.StudentGrade;
import school.hei.haapi.endpoint.rest.validator.GradeValidator;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.GradeRepository;
import school.hei.haapi.service.ExamService;
import school.hei.haapi.service.GradeService;
import school.hei.haapi.service.UserService;

@Component
@AllArgsConstructor
public class GradeMapper {
  private final UserMapper userMapper;
  private final GradeService service;
  private final ExamService examService;
  private final UserService userService;
  private final GradeValidator validator;
  private final GradeRepository gradeRepository;

  public Grade toRest(school.hei.haapi.model.Grade grade) {
    return new Grade()
        .id(grade.getId())
        .createdAt(grade.getCreationDatetime())
        .score(grade.getScore())
        .updateDate(grade.getCreationDatetime());
  }

  public StudentGrade toRestStudentGrade(school.hei.haapi.model.Grade grade) {
    var getStudentGrade = new StudentGrade().grade(toRest(grade));
    getStudentGrade.setStudent(userMapper.toRestStudent(grade.getStudent()));

    return getStudentGrade;
  }

  public StudentGrade toRestStudentExamGrade(User student, Exam exam) {
    return new StudentGrade()
        .grade(
            toRest(
                exam.getGrades().stream()
                    .filter(grade -> grade.getStudent().getId().equals(student.getId()))
                    .findFirst()
                    .orElseThrow(
                        () ->
                            new NotFoundException(
                                "Student %s have no grade for the exam %s"
                                    .formatted(student.getId(), exam.getId())))))
        .student(userMapper.toRestStudent(student));
  }

  public school.hei.haapi.model.Grade toDomain(
      CrupdateGrade grade, String examId, String studentRef) {
    validator.accept(grade);

    Exam exam = examService.getExamById(examId);

    school.hei.haapi.model.Grade resultGrade =
        gradeRepository
            .getGradeByExamIdAndStudentRef(examId, studentRef)
            .orElse(
                service
                    .crupdateParticipantGrade(
                        List.of(
                            new school.hei.haapi.model.Grade(
                                exam, userService.findByRef(studentRef))))
                    .getFirst());

    resultGrade.setScore(grade.getScore() * exam.getCoefficient());
    return resultGrade;
  }
}
