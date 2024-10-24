package school.hei.haapi.endpoint.rest.controller;

import static java.util.stream.Collectors.*;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.ExamMapper;
import school.hei.haapi.endpoint.rest.model.ExamInfo;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.AwardedCourseService;
import school.hei.haapi.service.ExamService;

@RestController
@AllArgsConstructor
public class ExamController {
  private final ExamService examService;
  private final AwardedCourseService awardedCourseService;
  private final ExamMapper examMapper;

  @GetMapping(value = "/awarded_courses/{awarded_course_id}/exams")
  public List<ExamInfo> getAwardedCourseExams(
      @PathVariable("awarded_course_id") String awardedCourseId,
      @RequestParam(value = "page", defaultValue = "1") PageFromOne page,
      @RequestParam(value = "page_size", defaultValue = "15") BoundedPageSize pageSize) {
    return examService
        .getExamsFromAwardedCourseIdAndGroupId(awardedCourseId, page, pageSize)
        .stream()
        .map(examMapper::toRest)
        .collect(toList());
  }

  @PutMapping(value = "/awarded_courses/{awarded_course_id}/exams")
  public List<ExamInfo> createOrUpdateExams(
      @PathVariable("awarded_course_id") String awardedCourseId,
      @RequestBody List<ExamInfo> examInfos) {
    List<Exam> exams =
        examService.updateOrSaveAll(
            examInfos.stream()
                .map(
                    examInfo ->
                        examMapper.toDomain(
                            examInfo, awardedCourseService.getById(awardedCourseId)))
                .collect(toList()));
    return exams.stream().map(examMapper::toRest).collect(toList());
  }

  @GetMapping(value = "/awarded_courses/{awarded_course_id}/exams/{exam_id}")
  public ExamInfo getExamById(
      @PathVariable("awarded_course_id") String awardedCourseId,
      @PathVariable("exam_id") String examId) {
    return examMapper.toRest(examService.getExamsByIdAndAwardedCourseId(examId, awardedCourseId));
  }
}
