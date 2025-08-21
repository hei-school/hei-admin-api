package school.hei.haapi.endpoint.rest.mapper;

import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateGrade;
import school.hei.haapi.endpoint.rest.model.Grade;
import school.hei.haapi.endpoint.rest.model.StudentGrade;
import school.hei.haapi.endpoint.rest.model.UpdateGrade;
import school.hei.haapi.endpoint.rest.validator.GradeValidator;
import school.hei.haapi.endpoint.rest.validator.UpdateGradeValidator;
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
  private final GradeValidator gradeValidator;
  private final UpdateGradeValidator updateGradeValidator;
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
        .score(grade.getScore())
        .updateDate(grade.getUpdateDatetime());
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

  public school.hei.haapi.model.Grade toDomain(CreateGrade grade, String examId, String studentId) {
    gradeValidator.accept(grade);

    var student = userService.findById(studentId);
    return gradeRepository
        .getGradeByExamIdAndStudentRef(examId, student.getRef())
        .orElse(
            new school.hei.haapi.model.Grade(
                examService.getExamById(examId), student, grade.getScore()));
  }

  public school.hei.haapi.model.notEntity.UpdateGrade toDomain(
      UpdateGrade grade, String examId, String studentRef) {
    updateGradeValidator.accept(grade);

    return new school.hei.haapi.model.notEntity.UpdateGrade(
        new school.hei.haapi.model.Grade(
            examService.getExamById(examId),
            userService.findByRef(studentRef),
            grade.getGrade().getScore()),
        userService.findByRef(studentRef),
        grade.getComment(),
        examService.getExamById(examId));
  }
}
