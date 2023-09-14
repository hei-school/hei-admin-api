package school.hei.haapi.endpoint.rest.mapper;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.ExamDetail;
import school.hei.haapi.endpoint.rest.model.ExamInfo;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.endpoint.rest.model.StudentExamGrade;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.AwardedCourse;
import school.hei.haapi.model.User;

@Component
@AllArgsConstructor
public class ExamMapper {
  private final GradeMapper gradeMapper;

  public ExamInfo toRestExamInfo(Exam exam) {
    return new ExamInfo().id(exam.getId())
        .coefficient(exam.getCoefficient()).title(exam.getTitle())
        .examinationDate(exam.getExaminationDate().atZone(ZoneId.systemDefault()).toInstant());
  }

  public Exam examInfoToDomain(ExamInfo examInfo) {
    return Exam.builder().id(examInfo.getId())
            .coefficient(examInfo.getCoefficient()).title(examInfo.getTitle())
            .examinationDate(examInfo.getExaminationDate().atZone(ZoneId.systemDefault()).toInstant()).build();
  }


}
