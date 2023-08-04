package school.hei.haapi.endpoint.rest.mapper;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.ExamDetail;
import school.hei.haapi.endpoint.rest.model.ExamInfo;
import school.hei.haapi.endpoint.rest.model.StudentExamGrade;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.StudentCourse;

@Component
@AllArgsConstructor
public class ExamMapper {
  private final GradeMapper gradeMapper;

  public ExamInfo toRestExamInfo(Exam exam) {
    return new ExamInfo().id(exam.getId())
        .coefficient(exam.getCoefficient()).title(exam.getCourse().getName())
        .examinationDate(exam.getExaminationDate().atZone(ZoneId.systemDefault()).toInstant());
  }

  public ExamDetail toRestExamDetail(Exam exam, List<Grade> grades) {
    return new ExamDetail().id(exam.getId()).coefficient(exam.getCoefficient())
        .title(exam.getTitle())
        .examinationDate(exam.getExaminationDate().atZone(ZoneId.systemDefault()).toInstant())
        .participants(grades.stream()
            .map(grade -> gradeMapper.toRestStudentGrade(grade,
                grade.getStudentCourse().getStudent()))
            .collect(Collectors.toList()));
  }

  public StudentExamGrade toRestExamExamGrade(Exam exam, StudentCourse studentCourse) {
    Optional<Grade> optionalGrade =
        studentCourse.getGrades().stream().filter(grade -> grade.getExam() == exam).collect(
            Collectors.toList()).stream().findFirst();
    Grade grade = optionalGrade.get();
    return new StudentExamGrade().id(exam.getId()).examinationDate(exam.getExaminationDate())
        .title(exam.getTitle()).coefficient(exam.getCoefficient())
        .grade(gradeMapper.toRest(grade));
  }
}
