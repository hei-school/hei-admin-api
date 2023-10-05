package school.hei.haapi.endpoint.rest.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.ExamMapper;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.ExamDetail;
import school.hei.haapi.endpoint.rest.model.ExamInfo;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.AwardedCourseService;
import school.hei.haapi.service.ExamService;
import school.hei.haapi.service.GradeService;

@RestController
@AllArgsConstructor

public class ExamController {
  private final ExamService examService;
  private final AwardedCourseService awardedCourseService;
  private final ExamMapper examMapper;

  @GetMapping(value = "/groups/{group_id}/awarded_courses/{awarded_course_id}/exams")
  public List<ExamInfo> getAwardedCourseExams(@PathVariable("group_id") String groupId,
                                              @PathVariable("awarded_course_id")
                                              String awardedCourseId,
                                              @RequestParam(value = "page", defaultValue = "1")
                                              PageFromOne page,
                                              @RequestParam(value = "page_size", defaultValue = "15")
                                              BoundedPageSize pageSize) {
    return examService.getExamsFromAwardedCourseIdAndGroupId(groupId, awardedCourseId, page,
        pageSize).stream().map(examMapper::toRestExamInfo).collect(Collectors.toList());
  }

  @PutMapping(value = "/groups/{group_id}/awarded_courses/{awarded_course_id}/exams")
  public List<ExamInfo> createOrUpdateExams(@PathVariable("group_id") String groupId,
                                            @PathVariable("awarded_course_id")
                                            String awardedCourseId,
                                            @RequestBody List<ExamInfo> examInfos) {
    List<Exam> exams = examService.updateOrSaveAll(examInfos.stream().map(
        examInfo -> examMapper.examInfoToDomain(examInfo,
            awardedCourseService.getById(awardedCourseId, groupId))).collect(Collectors.toList()));
    return exams.stream().map(examMapper::toRestExamInfo).collect(Collectors.toList());
  }

  @GetMapping(value = "/groups/{group_id}/awarded_courses/{awarded_course_id}/exams/{exam_id}")
  public ExamInfo getExamById(@PathVariable("group_id") String groupId,
                              @PathVariable("awarded_course_id") String awardedCourseId,
                              @PathVariable("exam_id") String examId) {
    return examMapper.toRestExamInfo(
        examService.getExamsByIdAndGroupIdAndAwardedCourseId(examId, awardedCourseId, groupId));
  }
}

