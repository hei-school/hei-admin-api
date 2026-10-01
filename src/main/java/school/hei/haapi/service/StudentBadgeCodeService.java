package school.hei.haapi.service;

import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.PRESENT;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.endpoint.rest.model.AttendanceStatus;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.model.StudentBadge;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.EventParticipantRepository;
import school.hei.haapi.repository.StudentBadgeRepository;
import school.hei.haapi.service.utils.AcademicYear;

@Service
@AllArgsConstructor
public class StudentBadgeCodeService {
  private final StudentBadgeRepository studentBadgeRepository;
  private final EventParticipantRepository eventParticipantRepository;

  @Transactional
  public StudentBadge getOrCreateBadge(User student, AcademicYear academicYear) {
    return studentBadgeRepository
        .findByStudentIdAndAcademicYearAndRevocationDatetimeIsNull(
            student.getId(), academicYear.label())
        .orElseGet(
            () ->
                studentBadgeRepository.save(
                    StudentBadge.builder()
                        .student(student)
                        .publicId(newPublicId())
                        .academicYear(academicYear.label())
                        .expirationDatetime(academicYear.badgeExpiration())
                        .build()));
  }

  public StudentBadge getByPublicId(String publicId) {
    return studentBadgeRepository
        .findByPublicId(publicId)
        .orElseThrow(() -> new NotFoundException("Student #" + publicId + " does not exist"));
  }

  public StudentBadge getActiveBadgeOfStudent(String studentId) {
    return studentBadgeRepository
        .findFirstByStudentIdAndRevocationDatetimeIsNullAndExpirationDatetimeAfterOrderByExpirationDatetimeDesc(
            studentId, Instant.now())
        .orElseThrow(() -> new NotFoundException("Student #" + studentId + " has no active badge"));
  }

  @Transactional
  public StudentBadge revokeActiveBadgeOfStudent(String studentId) {
    StudentBadge badge = getActiveBadgeOfStudent(studentId);
    badge.setRevocationDatetime(Instant.now());
    return studentBadgeRepository.save(badge);
  }

  @Transactional
  public StudentBadge revoke(String publicId) {
    StudentBadge badge = getByPublicId(publicId);
    if (!badge.isRevoked()) {
      badge.setRevocationDatetime(Instant.now());
    }
    return studentBadgeRepository.save(badge);
  }

  @Transactional
  public EventParticipant checkAttendance(
      String eventId, String publicId, AttendanceStatus attendanceStatus) {
    StudentBadge badge = getByPublicId(publicId);
    if (badge.isRevoked()) {
      throw new BadRequestException("Badge of student #" + publicId + " has been revoked");
    }
    if (badge.isExpiredAt(Instant.now())) {
      throw new BadRequestException(
          "Badge of student #" + publicId + " has expired (" + badge.getAcademicYear() + ")");
    }
    EventParticipant participant =
        eventParticipantRepository
            .findEventParticipantByParticipantIdAndEventId(badge.getStudent().getId(), eventId)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "Student #" + publicId + " is not a participant of event #" + eventId));
    participant.setStatus(attendanceStatus == null ? PRESENT : attendanceStatus);
    return eventParticipantRepository.save(participant);
  }

  private String newPublicId() {
    String publicId;
    do {
      publicId = UUID.randomUUID().toString();
    } while (studentBadgeRepository.existsByPublicId(publicId));
    return publicId;
  }
}
