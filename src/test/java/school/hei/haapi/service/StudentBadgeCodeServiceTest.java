package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.LATE;
import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.PRESENT;
import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.UNCHECKED;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.model.StudentBadge;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.EventParticipantRepository;
import school.hei.haapi.repository.StudentBadgeRepository;
import school.hei.haapi.service.utils.AcademicYear;

class StudentBadgeCodeServiceTest {
  private static final String EVENT_ID = "event1_id";
  private static final String PUBLIC_ID = "7c1e4b9a-2f3d-4e8a-9b6c-1d2e3f4a5b6c";
  private static final AcademicYear YEAR = AcademicYear.parse("2026 - 2027");
  private StudentBadgeRepository studentBadgeRepository;
  private EventParticipantRepository eventParticipantRepository;
  private StudentBadgeCodeService subject;

  @BeforeEach
  void setUp() {
    studentBadgeRepository = mock(StudentBadgeRepository.class);
    eventParticipantRepository = mock(EventParticipantRepository.class);
    when(studentBadgeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(eventParticipantRepository.save(any()))
        .thenAnswer(invocation -> invocation.getArgument(0));
    subject = new StudentBadgeCodeService(studentBadgeRepository, eventParticipantRepository);
  }

  @Test
  void reprint_of_same_academic_year_reuses_badge() {
    User student = student();
    StudentBadge active = badge(student, inOneYear());
    when(studentBadgeRepository.findByStudentIdAndAcademicYearAndRevocationDatetimeIsNull(
            student.getId(), "2026 - 2027"))
        .thenReturn(Optional.of(active));

    assertSame(active, subject.getOrCreateBadge(student, YEAR));
    verify(studentBadgeRepository, never()).save(any());
  }

  @Test
  void first_print_of_academic_year_creates_badge_expiring_at_its_end() {
    User student = student();
    when(studentBadgeRepository.findByStudentIdAndAcademicYearAndRevocationDatetimeIsNull(
            eq(student.getId()), anyString()))
        .thenReturn(Optional.empty());
    when(studentBadgeRepository.existsByPublicId(anyString())).thenReturn(false);

    StudentBadge created = subject.getOrCreateBadge(student, YEAR);

    assertSame(student, created.getStudent());
    assertEquals(4, UUID.fromString(created.getPublicId()).version());
    assertNotEquals(student.getId(), created.getPublicId());
    assertEquals("2026 - 2027", created.getAcademicYear());
    assertEquals(YEAR.badgeExpiration(), created.getExpirationDatetime());
  }

  @Test
  void expired_badge_is_not_valid_and_cannot_check_attendance() {
    StudentBadge expired = badge(student(), Instant.now().minus(1, ChronoUnit.DAYS));
    when(studentBadgeRepository.findByPublicId(PUBLIC_ID)).thenReturn(Optional.of(expired));

    assertFalse(expired.isValidAt(Instant.now()));
    assertThrows(
        BadRequestException.class, () -> subject.checkAttendance(EVENT_ID, PUBLIC_ID, PRESENT));
  }

  @Test
  void revoked_badge_cannot_check_attendance() {
    StudentBadge revoked = badge(student(), inOneYear());
    when(studentBadgeRepository.findByPublicId(PUBLIC_ID)).thenReturn(Optional.of(revoked));

    subject.revoke(PUBLIC_ID);

    assertTrue(revoked.isRevoked());
    assertFalse(revoked.isValidAt(Instant.now()));
    assertThrows(
        BadRequestException.class, () -> subject.checkAttendance(EVENT_ID, PUBLIC_ID, PRESENT));
  }

  @Test
  void scan_marks_student_present_by_default() {
    User student = student();
    when(studentBadgeRepository.findByPublicId(PUBLIC_ID))
        .thenReturn(Optional.of(badge(student, inOneYear())));
    EventParticipant participant =
        EventParticipant.builder().participant(student).status(UNCHECKED).build();
    when(eventParticipantRepository.findEventParticipantByParticipantIdAndEventId(
            student.getId(), EVENT_ID))
        .thenReturn(Optional.of(participant));

    assertEquals(PRESENT, subject.checkAttendance(EVENT_ID, PUBLIC_ID, null).getStatus());
    assertEquals(LATE, subject.checkAttendance(EVENT_ID, PUBLIC_ID, LATE).getStatus());
  }

  @Test
  void scan_ko_when_student_is_not_participant_or_public_id_unknown() {
    User student = student();
    when(studentBadgeRepository.findByPublicId(PUBLIC_ID))
        .thenReturn(Optional.of(badge(student, inOneYear())));
    when(eventParticipantRepository.findEventParticipantByParticipantIdAndEventId(
            student.getId(), EVENT_ID))
        .thenReturn(Optional.empty());
    when(studentBadgeRepository.findByPublicId("unknown")).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> subject.checkAttendance(EVENT_ID, PUBLIC_ID, null));
    assertThrows(NotFoundException.class, () -> subject.getByPublicId("unknown"));
  }

  @Test
  void remove_active_badge_of_student() {
    User student = student();
    StudentBadge active = badge(student, inOneYear());
    when(studentBadgeRepository
            .findFirstByStudentIdAndRevocationDatetimeIsNullAndExpirationDatetimeAfterOrderByExpirationDatetimeDesc(
                eq(student.getId()), any()))
        .thenReturn(Optional.of(active))
        .thenReturn(Optional.empty());

    StudentBadge removed = subject.revokeActiveBadgeOfStudent(student.getId());

    assertTrue(removed.isRevoked());
    // nothing left to remove: the next print creates a new badge
    assertThrows(NotFoundException.class, () -> subject.getActiveBadgeOfStudent(student.getId()));
  }

  private static Instant inOneYear() {
    return Instant.now().plus(365, ChronoUnit.DAYS);
  }

  private static User student() {
    User student = new User();
    student.setId("student1_id");
    student.setRef("STD26001");
    return student;
  }

  private static StudentBadge badge(User student, Instant expiration) {
    return StudentBadge.builder()
        .student(student)
        .publicId(PUBLIC_ID)
        .academicYear("2026 - 2027")
        .expirationDatetime(expiration)
        .creationDatetime(Instant.parse("2026-10-01T00:00:00Z"))
        .build();
  }
}
