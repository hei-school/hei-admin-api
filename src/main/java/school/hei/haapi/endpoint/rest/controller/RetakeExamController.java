package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.mapper.RetakeExamMapper;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.CrupdateRetakeExam;
import school.hei.haapi.endpoint.rest.model.RetakeExam;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.endpoint.rest.model.StudentRetakeExam;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.RetakeExamStatus;
import school.hei.haapi.service.RetakeExamService;

@RestController
@AllArgsConstructor
public class RetakeExamController {
  private final RetakeExamService retakeExamService;
  private final RetakeExamMapper retakeExamMapper;
  private final CourseMapper courseMapper;
  private final UserMapper userMapper;

  @PutMapping("/retake_exam_sessions/{session_id}/retake_exams")
  public List<StudentRetakeExam> createOrUpdateRetakeExam(
      @PathVariable("session_id") String sessionId,
      @RequestBody List<CrupdateRetakeExam> crupdateRetakeExams) {
    return retakeExamService
        .crupdateRetakeExams(retakeExamMapper.toDomainList(crupdateRetakeExams))
        .stream()
        .map(retakeExamMapper::toStudentRetakeRest)
        .toList();
  }

  @GetMapping("students/{student_id}/sessions/{session_id}/retake_exams")
  public List<RetakeExam> getStudentRetakeExamBySession(
      @PathVariable("student_id") String studentId,
      @PathVariable("session_id") String sessionId,
      @RequestParam(value = "page", defaultValue = "1") PageFromOne page,
      @RequestParam(value = "page_size", defaultValue = "15") BoundedPageSize pageSize
  ) {
    return retakeExamMapper.toRestList(
        retakeExamService.getStudentRetakeExams(sessionId, studentId, page, pageSize));
  }

  @GetMapping("/retake_exams")
  public List<StudentRetakeExam> getRetakeExamBySessionId(
      @RequestParam(value = "retake_exam_status", required = false) List<RetakeExamStatus> statuses,
      @RequestParam(value = "page", defaultValue = "1") PageFromOne page,
      @RequestParam(value = "page_size", defaultValue = "15") BoundedPageSize pageSize) {
    return retakeExamMapper.toStudentRetakeRestList(
        retakeExamService.getAllRetakeExams(statuses, page, pageSize));
  }

  @GetMapping("/retake_exam_sessions/{session_id}/retake_exam_courses")
  public List<Course> getRetakeExamCoursesBySessionId(
      @PathVariable("session_id") String sessionId,
      @RequestParam(value = "course_code", required = false) String courseCode,
      @RequestParam(value = "page", defaultValue = "1") PageFromOne page,
      @RequestParam(value = "page_size", defaultValue = "15") BoundedPageSize pageSize) {
    return courseMapper.toRests(
        retakeExamService.getAllRetakeExamCoursesBySessionId(
            sessionId, courseCode, page, pageSize));
  }

  @GetMapping("/retake_exam_sessions/{session_id}/retake_exam_courses/{course_id}/participants")
  public List<Student> getRetakeExamParticipantByCourseIdAndSessionId(
      @PathVariable("session_id") String sessionId,
      @PathVariable("course_id") String courseId,
      @RequestParam(value = "student_ref", required = false) String studentRef,
      @RequestParam(value = "page", defaultValue = "1") PageFromOne page,
      @RequestParam(value = "page_size", defaultValue = "15") BoundedPageSize pageSize) {
    return userMapper.toRestStudents(
        retakeExamService.getAllRetakeExamParticipantByCourseAndBySessionId(
            sessionId, courseId, studentRef, page, pageSize));
  }
}
