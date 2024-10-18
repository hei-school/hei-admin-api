package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.ExamInfo;
import school.hei.haapi.model.AwardedCourse;
import school.hei.haapi.model.Exam;

@Component
@AllArgsConstructor
public class ExamMapper {
  private AwardedCourseMapper awardedCourseMapper;
  public ExamInfo toRest(Exam exam) {
    return new ExamInfo()
        .id(exam.getId())
        .coefficient(exam.getCoefficient())
        .title(exam.getTitle())
        .examinationDate(exam.getExaminationDate())
        .awardedCourse(awardedCourseMapper.toRest(exam.getAwardedCourse()));
  }

  public Exam toDomain(ExamInfo examInfo, AwardedCourse awardedCourse) {
    return Exam.builder()
        .id(examInfo.getId())
        .coefficient(examInfo.getCoefficient())
        .title(examInfo.getTitle())
        .examinationDate(examInfo.getExaminationDate())
        .awardedCourse(awardedCourse)
        .build();
  }
}
