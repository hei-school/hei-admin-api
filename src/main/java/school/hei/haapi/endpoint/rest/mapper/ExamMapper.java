package school.hei.haapi.endpoint.rest.mapper;

import java.time.ZoneId;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.ExamInfo;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.AwardedCourse;

@Component
@AllArgsConstructor
public class ExamMapper {

  public ExamInfo toRest(Exam exam) {
    return new ExamInfo()
        .id(exam.getId())
        .coefficient(exam.getCoefficient())
        .title(exam.getTitle())
        .examinationDate(exam.getExaminationDate().atZone(ZoneId.systemDefault()).toInstant())
        .awardedCourseId(exam.getAwardedCourse().getId());
  }

  public Exam toDomain(ExamInfo examInfo, AwardedCourse awardedCourse) {
    return Exam.builder()
        .id(examInfo.getId())
        .coefficient(examInfo.getCoefficient())
        .title(examInfo.getTitle())
        .examinationDate(examInfo.getExaminationDate().atZone(ZoneId.systemDefault()).toInstant())
        .awardedCourse(awardedCourse).build();
  }


}
