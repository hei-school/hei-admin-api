package school.hei.haapi.service;

import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.PRESENT;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
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
  private static final Pattern PUBLIC_ID =
      Pattern.compile(
          "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
  private static final String BADGE_NOT_FOUND = "Badge not found";

  private final StudentBadgeRepository studentBadgeRepository;
  private final EventParticipantRepository eventParticipantRepository;

  public boolean hasActiveBadge(User student, AcademicYear academicYear) {
    var studentId = student.getId();
    return studentBadgeRepository
            .findByStudentIdAndAcademicYearAndRevocationDatetimeIsNull(
                studentId, academicYear.label())
            .isPresent()
        || studentBadgeRepository
            .existsByStudentIdAndExpirationDatetimeIsNullAndRevocationDatetimeIsNull(studentId);
  }

  public StudentBadge newBadge(User student, AcademicYear academicYear, boolean withoutExpiration) {
    if (hasActiveBadge(student, academicYear)) {
      throw new BadRequestException(
          "Student #"
              + student.getRef()
              + " already has an active badge for "
              + academicYear.label()
              + ": remove it before printing a new one");
    }
    return StudentBadge.builder()
        .student(student)
        .publicId(newPublicId())
        .academicYear(academicYear.label())
        .expirationDatetime(withoutExpiration ? null : academicYear.badgeExpiration())
        .build();
  }

  @Transactional
  public List<StudentBadge> saveBadges(List<StudentBadge> badges) {
    return studentBadgeRepository.saveAll(badges);
  }

  public StudentBadge getByPublicId(String publicId) {
    if (publicId == null || !PUBLIC_ID.matcher(publicId).matches()) {
      throw new NotFoundException(BADGE_NOT_FOUND);
    }
    return studentBadgeRepository
        .findByPublicId(publicId.toLowerCase())
        .orElseThrow(() -> new NotFoundException(BADGE_NOT_FOUND));
  }

  public StudentBadge getValidBadge(String publicId) {
    var badge = getByPublicId(publicId);
    if (badge.isRevoked()) {
      throw new BadRequestException("The badge has been revoked");
    }
    if (badge.isExpiredAt(Instant.now())) {
      throw new BadRequestException("The badge has expired (" + badge.getAcademicYear() + ")");
    }
    return badge;
  }

  public StudentBadge getActiveBadgeOfStudent(String studentId) {
    return studentBadgeRepository
        .findCurrentBadgeOfStudent(studentId, Instant.now())
        .orElseThrow(() -> new NotFoundException("Student #" + studentId + " has no active badge"));
  }

  @Transactional
  public StudentBadge revokeActiveBadgeOfStudent(String studentId) {
    return revoke(getActiveBadgeOfStudent(studentId));
  }

  @Transactional
  public StudentBadge revoke(String publicId) {
    return revoke(getByPublicId(publicId));
  }

  @Transactional
  public EventParticipant checkAttendance(
      String eventId, String publicId, AttendanceStatus attendanceStatus) {
    var badge = getValidBadge(publicId);
    var participant =
        eventParticipantRepository
            .findEventParticipantByParticipantIdAndEventId(badge.getStudent().getId(), eventId)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "The student of the badge is not a participant of event #" + eventId));
    participant.setStatus(attendanceStatus == null ? PRESENT : attendanceStatus);
    return eventParticipantRepository.save(participant);
  }

  private StudentBadge revoke(StudentBadge badge) {
    if (!badge.isRevoked()) {
      badge.setRevocationDatetime(Instant.now());
    }
    return studentBadgeRepository.save(badge);
  }

  private String newPublicId() {
    String publicId;
    do {
      publicId = UUID.randomUUID().toString();
    } while (studentBadgeRepository.existsByPublicId(publicId));
    return publicId;
  }
}
