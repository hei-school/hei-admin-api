package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.RetakeExamMapper;
import school.hei.haapi.endpoint.rest.model.CrupdateRetakeExam;
import school.hei.haapi.endpoint.rest.model.RetakeExam;
import school.hei.haapi.endpoint.rest.model.StudentRetakeExam;
import school.hei.haapi.service.RetakeExamService;

@RestController
@RequestMapping
@AllArgsConstructor
public class RetakeExamController {
  private final RetakeExamService retakeExamService;
  private final RetakeExamMapper retakeExamMapper;

  @PutMapping("/retake_exam_sessions/{session_id}/retakeExams")
  public List<StudentRetakeExam> createOrUpdateRetakeExam(
      @PathVariable("session_id") String sessionId,
      @RequestBody List<CrupdateRetakeExam> crupdateRetakeExams) {
    return retakeExamService.crupdateRetakeExams(sessionId, crupdateRetakeExams).stream()
        .map(retakeExamMapper::toStudentRetakeRest)
        .toList();
  }

  @GetMapping("students/{student_id}/sessions/{session_id}/retakeExams")
  public List<RetakeExam> getStudentRetakeExamBySession(
      @PathVariable("student_id") String studentId, @PathVariable("session_id") String sessionId) {
    return retakeExamMapper.toRestList(
        retakeExamService.getStudentRetakeExams(sessionId, studentId));
  }
}
