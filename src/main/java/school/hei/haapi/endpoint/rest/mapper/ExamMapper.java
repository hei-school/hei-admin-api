package school.hei.haapi.endpoint.rest.mapper;

import java.time.ZoneId;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.ExamDetail;
import school.hei.haapi.endpoint.rest.model.ExamInfo;
import school.hei.haapi.model.Exam;
import school.hei.haapi.service.GradeService;

@Component
@AllArgsConstructor
public class ExamMapper {
  private final GradeService gradeService;
  private final GradeMapper gradeMapper;

  public ExamInfo toRestExamInfo(Exam exam) {
    ExamInfo examInfo = new ExamInfo();
    examInfo.setId(exam.getId());
    examInfo.setCoefficient(exam.getCoefficient());
    examInfo.setTitle(exam.getCourse().getName());
    examInfo.setExaminationDate(
        exam.getExaminationDate().atZone(ZoneId.systemDefault()).toInstant());
    return examInfo;
  }

  public ExamDetail toRestExamDetail(Exam exam) {
    ExamDetail examDetail = new ExamDetail();
    examDetail.setId(exam.getId());
    examDetail.setCoefficient(exam.getCoefficient());
    examDetail.setTitle(exam.getTitle());
    examDetail.setExaminationDate(
        exam.getExaminationDate().atZone(ZoneId.systemDefault()).toInstant());
    examDetail.setParticipants(gradeService.getAllGradesByExamId(exam.getId()).stream()
        .map(gradeMapper::toRestStudentGrade).collect(Collectors.toList()));
    return examDetail;
  }


}
