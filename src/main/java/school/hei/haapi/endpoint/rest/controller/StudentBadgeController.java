package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.EventParticipantMapper;
import school.hei.haapi.endpoint.rest.mapper.StudentBadgeMapper;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.endpoint.rest.model.AttendanceStatus;
import school.hei.haapi.endpoint.rest.model.EventParticipant;
import school.hei.haapi.endpoint.rest.model.PublicStudent;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.service.StudentBadgeCodeService;

@RestController
@AllArgsConstructor
public class StudentBadgeController {
  private final StudentBadgeCodeService studentBadgeCodeService;
  private final StudentBadgeMapper studentBadgeMapper;
  private final UserMapper userMapper;
  private final EventParticipantMapper eventParticipantMapper;

  @GetMapping("/students/badges/{id}")
  public PublicStudent getPublicStudent(@PathVariable(name = "id") String publicId) {
    return studentBadgeMapper.toRest(studentBadgeCodeService.getByPublicId(publicId));
  }

  @GetMapping("/students/badges/{id}/student")
  public Student getStudentByPublicId(@PathVariable(name = "id") String publicId) {
    return userMapper.toRestStudent(studentBadgeCodeService.getByPublicId(publicId).getStudent());
  }

  @PutMapping("/students/badges/{id}/revocation")
  public PublicStudent revokeStudentBadge(@PathVariable(name = "id") String publicId) {
    return studentBadgeMapper.toRest(studentBadgeCodeService.revoke(publicId));
  }

  @GetMapping("/students/{id}/badge")
  public PublicStudent getStudentActiveBadge(@PathVariable(name = "id") String studentId) {
    return studentBadgeMapper.toRest(studentBadgeCodeService.getActiveBadgeOfStudent(studentId));
  }

  @PutMapping("/students/{id}/badge/revocation")
  public PublicStudent revokeStudentActiveBadge(@PathVariable(name = "id") String studentId) {
    return studentBadgeMapper.toRest(studentBadgeCodeService.revokeActiveBadgeOfStudent(studentId));
  }

  @PutMapping("/students/badges/{id}/events/{event_id}/attendance")
  public EventParticipant checkEventAttendanceByPublicId(
      @PathVariable(name = "event_id") String eventId,
      @PathVariable(name = "id") String publicId,
      @RequestParam(name = "status", required = false) AttendanceStatus status) {
    return eventParticipantMapper.toRest(
        studentBadgeCodeService.checkAttendance(eventId, publicId, status));
  }
}
