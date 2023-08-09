package school.hei.haapi.endpoint.rest.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.ExamMapper;
import school.hei.haapi.endpoint.rest.model.ExamDetail;
import school.hei.haapi.endpoint.rest.model.ExamInfo;
import school.hei.haapi.model.Exam;
import school.hei.haapi.service.ExamService;
import school.hei.haapi.service.GradeService;

@RestController
@AllArgsConstructor

public class ExamController {
  private final ExamService examService;
  private final GradeService gradeService;
  private final ExamMapper examMapper;

  @GetMapping(value = "/courses/{course_id}/exams")
  public List<ExamInfo> getCourseExams(@PathVariable("course_id") String courseId) {
    List<Exam> exams = examService.getCourseExams(courseId);
    return exams.stream().map(examMapper::toRestExamInfo).collect(Collectors.toList());
  }

  @GetMapping(value = "/courses/{course_id}/exams/{exam_id}/details")
  public ExamDetail getExamDetails(
      @PathVariable("course_id") String courseId, @PathVariable("exam_id") String examId) {
    return examMapper.toRestExamDetail(examService.getExamById(examId, courseId),
        gradeService.getAllGradesByExamId(examId));
  }


}

