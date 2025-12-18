package school.hei.haapi.endpoint.rest.mapper;

import static java.time.Instant.now;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateGrade;
import school.hei.haapi.endpoint.rest.model.Grade;
import school.hei.haapi.endpoint.rest.model.GradeValidRow;
import school.hei.haapi.endpoint.rest.model.StudentGrade;
import school.hei.haapi.endpoint.rest.model.UpdateGrade;
import school.hei.haapi.endpoint.rest.validator.GradeValidator;
import school.hei.haapi.endpoint.rest.validator.UpdateGradeValidator;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.dto.GradeImportDto;
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

  public school.hei.haapi.model.Grade toDomain(GradeImportDto grade, Exam exam) {
    var student = userService.findByRef(grade.getRef());
    return school.hei.haapi.model.Grade.builder()
        .score(grade.getScore())
        .exam(exam)
        .creationDatetime(now())
        .student(student)
        .build();
  }

  public List<school.hei.haapi.model.Grade> toDomainList(
      List<GradeImportDto> gradeImportDtos, String examId) {
    var exam = examService.getExamById(examId);
    return gradeImportDtos.stream()
        .map(gradeImportDto -> this.toDomain(gradeImportDto, exam))
        .toList();
  }

  public GradeValidRow toRestValidGrade(school.hei.haapi.model.Grade grade) {
    var ref = grade.getStudent().getRef();
    return new GradeValidRow().ref(ref).score(BigDecimal.valueOf(grade.getScore()));
  }

  public List<GradeValidRow> toRestListValidGrade(List<school.hei.haapi.model.Grade> grades) {
    return grades.stream().map(this::toRestValidGrade).toList();
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

  public school.hei.haapi.model.Grade toDomain(CreateGrade grade, String examId, String studentId) {
    gradeValidator.accept(grade);

    var student = userService.getById(studentId);
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

  public school.hei.haapi.model.notEntity.UpdateGrade toDomain(
      GradeImportDto gradeDto, String ref, Exam exam, String comment) {
    var student = userService.findByRef(ref);
    var grade = new school.hei.haapi.model.Grade(exam, student, gradeDto.getScore());
    return new school.hei.haapi.model.notEntity.UpdateGrade(grade, student, comment, exam);
  }

  public List<school.hei.haapi.model.notEntity.UpdateGrade> toDomainList(
      List<GradeImportDto> gradeDtos, String examId, String comment) {
    var exam = examService.getExamById(examId);
    return gradeDtos.stream()
        .map(gradeImportDto -> toDomain(gradeImportDto, gradeImportDto.getRef(), exam, comment))
        .toList();
  }
}
