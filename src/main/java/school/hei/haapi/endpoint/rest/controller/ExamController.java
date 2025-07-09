package school.hei.haapi.endpoint.rest.controller;

import static java.util.stream.Collectors.toList;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.ExamMapper;
import school.hei.haapi.endpoint.rest.model.CrupdateExam;
import school.hei.haapi.endpoint.rest.model.ExamInfo;
import school.hei.haapi.endpoint.rest.model.StudentExamGrade;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.exception.NotImplementedException;
import school.hei.haapi.service.CourseAssignmentService;
import school.hei.haapi.service.ExamService;

@RestController
@AllArgsConstructor
public class ExamController {
  private final ExamService examService;
  private final CourseAssignmentService awardedCourseService;
  private final ExamMapper examMapper;

  @GetMapping("/awarded_courses/{awarded_course_id}/exams")
  public List<ExamInfo> getExamsByAwardedCourse(
      @PathVariable(name = "awarded_course_id") String id,
      @RequestParam(value = "page", defaultValue = "1") PageFromOne page,
      @RequestParam(value = "page_size", defaultValue = "15") BoundedPageSize pageSize) {
    // TODO: Review this part, why it has test and passed then now this resources disapeared and
    // test failed
    throw new NotImplementedException("Resources are not implemented yet");
  }

  @GetMapping("/exams")
  public List<ExamInfo> getAllExams(
      @RequestParam(value = "page", defaultValue = "1") PageFromOne page,
      @RequestParam(value = "page_size", defaultValue = "15") BoundedPageSize pageSize,
      @RequestParam(value = "title", required = false) String title,
      @RequestParam(value = "course_code", required = false) String courseCode,
      @RequestParam(value = "group_ref", required = false) String groupRef,
      @RequestParam(value = "examination_date_from", required = false) Instant examinationDateFrom,
      @RequestParam(value = "examination_date_to", required = false) Instant examinationDateTo,
      @RequestParam(value = "awarded_course_id", required = false) String awardedCourseId) {
    return examMapper.toRestList(
        examService.getAllExams(
            page,
            pageSize,
            title,
            courseCode,
            groupRef,
            examinationDateFrom,
            examinationDateTo,
            awardedCourseId));
  }

  @GetMapping("/exams/{id}")
  public ExamInfo getExam(@PathVariable(name = "id") String id) {
    return examMapper.toRest(examService.getExamById(id));
  }

  @PutMapping("/exams")
  public ExamInfo createOrUpdateExamsInfos(@RequestBody CrupdateExam examInfo) {
    return examMapper.toRest(
        examService.updateOrSaveAll(List.of(examMapper.toDomain(examInfo))).getFirst());
  }

  @GetMapping(value = "/groups/{group_id}/awarded_courses/{awarded_course_id}/exams")
  public List<ExamInfo> getAwardedCourseExams(
      @PathVariable("group_id") String groupId,
      @PathVariable("awarded_course_id") String awardedCourseId,
      @RequestParam(value = "page", defaultValue = "1") PageFromOne page,
      @RequestParam(value = "page_size", defaultValue = "15") BoundedPageSize pageSize) {
    return examService
        .getExamsFromAwardedCourseIdAndGroupId(groupId, awardedCourseId, page, pageSize)
        .stream()
        .map(examMapper::toRest)
        .collect(toList());
  }

  @PutMapping(value = "/awarded_courses/{awarded_course_id}/exams")
  public List<ExamInfo> createOrUpdateExams(
      @PathVariable("awarded_course_id") String awardedCourseId,
      @RequestBody List<ExamInfo> examInfos) {
    return examService
        .updateOrSaveAll(
            examInfos.stream()
                .map(
                    examInfo ->
                        examMapper.toDomain(
                            examInfo, awardedCourseService.findById(awardedCourseId)))
                .collect(toList()))
        .stream()
        .map(examMapper::toRest)
        .collect(toList());
  }

  @GetMapping(value = "/groups/{group_id}/awarded_courses/{awarded_course_id}/exams/{exam_id}")
  public ExamInfo getExamById(
      @PathVariable("group_id") String groupId,
      @PathVariable("awarded_course_id") String awardedCourseId,
      @PathVariable("exam_id") String examId) {
    return examMapper.toRest(
        examService.getExamsByIdAndGroupIdAndAwardedCourseId(examId, awardedCourseId, groupId));
  }

  @GetMapping(value = "/courses/{course_id}/student/{student_id}/exams/grades")
  public List<StudentExamGrade> getStudentExamsGrade(
      @PathVariable(value = "course_id") String courseId,
      @PathVariable(value = "student_id") String studentId) {
    return examService.getExamsByCourseId(courseId).stream()
        .map(exam -> examMapper.toRestStudentExamGrade(studentId, exam))
        .toList();
  }
}
