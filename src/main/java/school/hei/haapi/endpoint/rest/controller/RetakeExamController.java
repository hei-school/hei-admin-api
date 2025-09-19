package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.RetakeExamMapper;
import school.hei.haapi.endpoint.rest.model.CrupdateRetakeExam;
import school.hei.haapi.endpoint.rest.model.RetakeExam;
import school.hei.haapi.service.RetakeExamService;

@RestController
@RequestMapping
public class RetakeExamController {
  @Autowired RetakeExamService retakeExamService;

  @Autowired RetakeExamMapper retakeExamMapper;

  @PutMapping("/retake_exam_sessions/{session_id}/retakeExams")
  public List<RetakeExam> retakeExams(
      @PathVariable("session_id") String sessionId, List<CrupdateRetakeExam> crupdateRetakeExams) {
    return retakeExamMapper.toRestList(
        retakeExamService.crupdateRetakeExams(sessionId, crupdateRetakeExams));
  }

  @GetMapping("students/{student_id}/sessions/{session_id}/retakeExams")
  public List<RetakeExam> getStudentRetakeExams(
      @PathVariable("student_id") String studentId, @PathVariable("session_id") String sessionId) {
    return retakeExamMapper.toRestList(
        retakeExamService.getStudentRetakeExams(sessionId, studentId));
  }
}
