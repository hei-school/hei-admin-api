package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateExam;
import school.hei.haapi.endpoint.rest.model.ExamInfo;
import school.hei.haapi.model.AwardedCourse;
import school.hei.haapi.model.Exam;
import school.hei.haapi.service.AwardedCourseService;

import java.util.List;

@Component
@AllArgsConstructor
public class ExamMapper {
  private AwardedCourseMapper awardedCourseMapper;
  private AwardedCourseService awardedCourseService;

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

  public Exam toDomain(CreateExam createExam) {
    AwardedCourse awardedCourse = awardedCourseService.findById(createExam.getAwardedCourseId());
    return Exam.builder()
            .id(createExam.getId())
            .coefficient(createExam.getCoefficient())
            .title(createExam.getTitle())
            .examinationDate(createExam.getExaminationDate())
            .awardedCourse(awardedCourse)
            .build();
  }

  public List<ExamInfo> toDomainList(List<Exam> examList){
    return examList.stream()
            .map(this::toRest)
            .toList();
  }
}
