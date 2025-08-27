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
import school.hei.haapi.endpoint.rest.model.Exam;
import school.hei.haapi.endpoint.rest.model.StudentExamGrade;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.exception.NotImplementedException;
import school.hei.haapi.service.CourseAssignmentService;
import school.hei.haapi.service.ExamService;
import school.hei.haapi.service.GradeService;

@RestController
@AllArgsConstructor
public class ExamController {
  private final ExamService examService;
  private final CourseAssignmentService courseAssignmentService;
  private final ExamMapper examMapper;
  private final GradeService gradeService;

  @GetMapping("/course_assignments/{course_assignment_id}/exams")
  public List<Exam> getExamsByAwardedCourse(
      @PathVariable(name = "course_assignment_id") String id,
      @RequestParam(value = "page", defaultValue = "1") PageFromOne page,
      @RequestParam(value = "page_size", defaultValue = "15") BoundedPageSize pageSize) {
    // TODO: Review this part, why it has test and passed then now this resources disapeared and
    // test failed
    throw new NotImplementedException("Resources are not implemented yet");
  }

  @GetMapping("/exams")
  public List<Exam> getAllExams(
      @RequestParam(value = "page", defaultValue = "1") PageFromOne page,
      @RequestParam(value = "page_size", defaultValue = "15") BoundedPageSize pageSize,
      @RequestParam(value = "title", required = false) String title,
      @RequestParam(value = "teacher_id", required = false) String teacherId,
      @RequestParam(value = "course_code", required = false) String courseCode,
      @RequestParam(value = "group_ref", required = false) String groupRef,
      @RequestParam(value = "examination_date_from", required = false) Instant examinationDateFrom,
      @RequestParam(value = "examination_date_to", required = false) Instant examinationDateTo,
      @RequestParam(value = "course_assignment_id", required = false) String courseAssignmentId) {
    return examMapper.toRestList(
        examService.getAllExams(
            page,
            pageSize,
            title,
            courseCode,
            teacherId,
            groupRef,
            examinationDateFrom,
            examinationDateTo,
            courseAssignmentId));
  }

  @GetMapping("/exams/{id}")
  public Exam getExam(@PathVariable(name = "id") String id) {
    return examMapper.toRest(examService.getExamById(id));
  }

  @PutMapping("/exams")
  public Exam createOrUpdateExamsInfos(@RequestBody CrupdateExam examInfo) {
    return examMapper.toRest(
        examService.updateOrSaveAll(List.of(examMapper.toDomain(examInfo))).getFirst());
  }

  @PutMapping(value = "/course_assignments/{course_assignment_id}/exams")
  public List<Exam> createOrUpdateExams(
      @PathVariable("course_assignment_id") String courseAssignmentId,
      @RequestBody List<Exam> examInfos) {
    return examService
        .updateOrSaveAll(
            examInfos.stream()
                .map(
                    examInfo ->
                        examMapper.toDomain(
                            examInfo,
                            courseAssignmentService.getCourseAssignmentById(courseAssignmentId)))
                .collect(toList()))
        .stream()
        .map(examMapper::toRest)
        .toList();
  }

  // TODO: remove the unnecessary path variables
  @GetMapping(value = "/course_assignments/{course_assignment_id}/exams/{exam_id}")
  public Exam getExamById(
      @PathVariable("course_assignment_id") String courseAssignmentId,
      @PathVariable("exam_id") String examId) {
    return examMapper.toRest(examService.getExamById(examId));
  }

  @GetMapping(value = "/courses/{course_id}/student/{student_id}/exams/grades")
  public List<StudentExamGrade> getStudentExamsGrade(
      @PathVariable(value = "course_id") String courseId,
      @PathVariable(value = "student_id") String studentId) {
    return gradeService.getGradesByStudentAndCourseId(studentId, courseId).stream()
        .map(examMapper::toRestStudentExamGrade)
        .toList();
  }
}
