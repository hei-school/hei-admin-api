package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.EventParticipantMapper;
import school.hei.haapi.endpoint.rest.mapper.StudentBadgeMapper;
import school.hei.haapi.endpoint.rest.model.AttendanceStatus;
import school.hei.haapi.endpoint.rest.model.BadgeOwner;
import school.hei.haapi.endpoint.rest.model.EventParticipant;
import school.hei.haapi.endpoint.rest.model.PublicStudent;
import school.hei.haapi.endpoint.rest.model.StudentSituation;
import school.hei.haapi.service.StudentBadgeCodeService;
import school.hei.haapi.service.StudentSituationService;

@RestController
@AllArgsConstructor
public class StudentBadgeController {
  private final StudentBadgeCodeService studentBadgeCodeService;
  private final StudentSituationService studentSituationService;
  private final StudentBadgeMapper studentBadgeMapper;
  private final EventParticipantMapper eventParticipantMapper;

  @GetMapping("/badges/{id}")
  public ResponseEntity<PublicStudent> getPublicStudent(
      @PathVariable(name = "id") String publicId) {
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .header("X-Robots-Tag", "noindex, nofollow")
        .body(studentBadgeMapper.toPublicRest(studentBadgeCodeService.getByPublicId(publicId)));
  }

  @GetMapping("/badges/{id}/student")
  public BadgeOwner getBadgeOwner(@PathVariable(name = "id") String publicId) {
    return studentBadgeMapper.toOwner(studentBadgeCodeService.getByPublicId(publicId));
  }

  @GetMapping("/badges/{id}/situation")
  public StudentSituation getBadgeStudentSituation(@PathVariable(name = "id") String publicId) {
    return studentSituationService.getSituationOfBadge(publicId);
  }

  @PutMapping("/badges/{id}/revocation")
  public PublicStudent revokeStudentBadge(@PathVariable(name = "id") String publicId) {
    return studentBadgeMapper.toRest(studentBadgeCodeService.revoke(publicId));
  }

  @PutMapping("/badges/{id}/events/{event_id}/attendance")
  public EventParticipant checkEventAttendanceByPublicId(
      @PathVariable(name = "event_id") String eventId,
      @PathVariable(name = "id") String publicId,
      @RequestParam(name = "status", required = false) AttendanceStatus status) {
    return eventParticipantMapper.toRest(
        studentBadgeCodeService.checkAttendance(eventId, publicId, status));
  }

  @GetMapping("/students/{id}/badge")
  public PublicStudent getStudentActiveBadge(@PathVariable(name = "id") String studentId) {
    return studentBadgeMapper.toRest(studentBadgeCodeService.getActiveBadgeOfStudent(studentId));
  }

  @PutMapping("/students/{id}/badge/revocation")
  public PublicStudent revokeStudentActiveBadge(@PathVariable(name = "id") String studentId) {
    return studentBadgeMapper.toRest(studentBadgeCodeService.revokeActiveBadgeOfStudent(studentId));
  }
}
