package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.model.CourseResult;
import school.hei.haapi.endpoint.rest.model.CourseResultStatus;
import school.hei.haapi.service.GradeResultService;

@RestController
@RequiredArgsConstructor
public class StudentRetakeExamList {
  private final GradeResultService gradeResultService;

  @GetMapping("/students/{student_id}/retake_exams")
  public List<CourseResult> getStudentRetakeExams(
      @PathVariable("student_id") String student_id,
      @RequestParam(defaultValue = "", required = false) CourseResultStatus status) {
    return gradeResultService.getStudentRetakeExams(student_id, status);
  }
}
