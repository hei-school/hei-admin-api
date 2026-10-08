package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import java.util.List;
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
  void no_second_badge_while_one_is_active() {
    var student = student();
    when(studentBadgeRepository.findByStudentIdAndAcademicYearAndRevocationDatetimeIsNull(
            student.getId(), "2026 - 2027"))
        .thenReturn(Optional.of(badge(student, inOneYear())));

    assertTrue(subject.hasActiveBadge(student, YEAR));
    assertThrows(BadRequestException.class, () -> subject.newBadge(student, YEAR, false));
    verify(studentBadgeRepository, never()).save(any());
  }

  @Test
  void badge_without_expiration_stays_active_the_following_years() {
    var student = student();
    when(studentBadgeRepository
            .existsByStudentIdAndExpirationDatetimeIsNullAndRevocationDatetimeIsNull(
                student.getId()))
        .thenReturn(true);

    var nextYear = AcademicYear.parse("2027 - 2028");
    assertTrue(subject.hasActiveBadge(student, nextYear));
    assertThrows(BadRequestException.class, () -> subject.newBadge(student, nextYear, true));
  }

  @Test
  void badge_from_the_third_year_of_licence_has_no_expiration() {
    var student = student();
    when(studentBadgeRepository.existsByPublicId(anyString())).thenReturn(false);

    var created = subject.newBadge(student, YEAR, true);

    assertNull(created.getExpirationDatetime());
    assertTrue(created.isValidAt(Instant.parse("2040-01-01T00:00:00Z")));
  }

  @Test
  void value_that_is_not_a_public_id_is_not_found_without_looking_for_it() {
    assertThrows(NotFoundException.class, () -> subject.getByPublicId("not-a-public-id"));
    assertThrows(NotFoundException.class, () -> subject.getByPublicId(null));
    verify(studentBadgeRepository, never()).findByPublicId(any());
  }

  @Test
  void first_print_of_academic_year_creates_badge_expiring_at_its_end() {
    var student = student();
    when(studentBadgeRepository.findByStudentIdAndAcademicYearAndRevocationDatetimeIsNull(
            eq(student.getId()), anyString()))
        .thenReturn(Optional.empty());
    when(studentBadgeRepository.existsByPublicId(anyString())).thenReturn(false);

    var created = subject.newBadge(student, YEAR, false);

    assertSame(student, created.getStudent());
    assertEquals(4, UUID.fromString(created.getPublicId()).version());
    assertNotEquals(student.getId(), created.getPublicId());
    assertEquals("2026 - 2027", created.getAcademicYear());
    assertEquals(YEAR.badgeExpiration(), created.getExpirationDatetime());
    verify(studentBadgeRepository, never()).save(any());
  }

  @Test
  void printed_badges_are_saved_all_at_once() {
    var badges = List.of(badge(student(), inOneYear()), badge(student(), inOneYear()));
    when(studentBadgeRepository.saveAll(badges)).thenReturn(badges);

    assertEquals(badges, subject.saveBadges(badges));
    verify(studentBadgeRepository).saveAll(badges);
  }

  @Test
  void expired_badge_is_not_valid_and_cannot_check_attendance() {
    var expired = badge(student(), Instant.now().minus(1, ChronoUnit.DAYS));
    when(studentBadgeRepository.findByPublicId(PUBLIC_ID)).thenReturn(Optional.of(expired));

    assertFalse(expired.isValidAt(Instant.now()));
    assertThrows(
        BadRequestException.class, () -> subject.checkAttendance(EVENT_ID, PUBLIC_ID, PRESENT));
  }

  @Test
  void revoked_badge_cannot_check_attendance() {
    var revoked = badge(student(), inOneYear());
    when(studentBadgeRepository.findByPublicId(PUBLIC_ID)).thenReturn(Optional.of(revoked));

    subject.revoke(PUBLIC_ID);

    assertTrue(revoked.isRevoked());
    assertFalse(revoked.isValidAt(Instant.now()));
    assertThrows(
        BadRequestException.class, () -> subject.checkAttendance(EVENT_ID, PUBLIC_ID, PRESENT));
  }

  @Test
  void scan_marks_student_present_by_default() {
    var student = student();
    when(studentBadgeRepository.findByPublicId(PUBLIC_ID))
        .thenReturn(Optional.of(badge(student, inOneYear())));
    var participant = EventParticipant.builder().participant(student).status(UNCHECKED).build();
    when(eventParticipantRepository.findEventParticipantByParticipantIdAndEventId(
            student.getId(), EVENT_ID))
        .thenReturn(Optional.of(participant));

    assertEquals(PRESENT, subject.checkAttendance(EVENT_ID, PUBLIC_ID, null).getStatus());
    assertEquals(LATE, subject.checkAttendance(EVENT_ID, PUBLIC_ID, LATE).getStatus());
  }

  @Test
  void scan_ko_when_student_is_not_participant_or_public_id_unknown() {
    var student = student();
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
    var student = student();
    var active = badge(student, inOneYear());
    when(studentBadgeRepository.findCurrentBadgeOfStudent(eq(student.getId()), any()))
        .thenReturn(Optional.of(active))
        .thenReturn(Optional.empty());

    var removed = subject.revokeActiveBadgeOfStudent(student.getId());

    assertTrue(removed.isRevoked());
    assertThrows(NotFoundException.class, () -> subject.getActiveBadgeOfStudent(student.getId()));
  }

  private static Instant inOneYear() {
    return Instant.now().plus(365, ChronoUnit.DAYS);
  }

  private static User student() {
    var student = new User();
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
