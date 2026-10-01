package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.LATE;
import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.PRESENT;
import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.UNCHECKED;

import java.time.Instant;
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

class StudentBadgeCodeServiceTest {
  private static final String EVENT_ID = "event1_id";
  private static final String PUBLIC_ID = "7c1e4b9a-2f3d-4e8a-9b6c-1d2e3f4a5b6c";
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
  void reprint_reuses_active_public_id() {
    User student = student();
    StudentBadge active = badge(student);
    when(studentBadgeRepository.findByStudentIdAndRevocationDatetimeIsNull(student.getId()))
        .thenReturn(Optional.of(active));

    assertSame(active, subject.getOrCreateActiveBadge(student));
    verify(studentBadgeRepository, never()).save(any());
  }

  @Test
  void first_print_creates_a_random_uuid_different_from_student_id() {
    User student = student();
    when(studentBadgeRepository.findByStudentIdAndRevocationDatetimeIsNull(student.getId()))
        .thenReturn(Optional.empty());
    when(studentBadgeRepository.existsByPublicId(anyString())).thenReturn(false);

    StudentBadge created = subject.getOrCreateActiveBadge(student);

    assertSame(student, created.getStudent());
    UUID publicId = UUID.fromString(created.getPublicId());
    assertEquals(4, publicId.version());
    assertNotEquals(student.getId(), created.getPublicId());
  }

  @Test
  void revoked_badge_cannot_check_attendance() {
    StudentBadge revoked = badge(student());
    when(studentBadgeRepository.findByPublicId(PUBLIC_ID)).thenReturn(Optional.of(revoked));

    subject.revoke(PUBLIC_ID);

    assertTrue(revoked.isRevoked());
    assertThrows(
        BadRequestException.class, () -> subject.checkAttendance(EVENT_ID, PUBLIC_ID, PRESENT));
  }

  @Test
  void scan_marks_student_present_by_default() {
    User student = student();
    when(studentBadgeRepository.findByPublicId(PUBLIC_ID)).thenReturn(Optional.of(badge(student)));
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
    when(studentBadgeRepository.findByPublicId(PUBLIC_ID)).thenReturn(Optional.of(badge(student)));
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
    StudentBadge active = badge(student);
    when(studentBadgeRepository.findByStudentIdAndRevocationDatetimeIsNull(student.getId()))
        .thenReturn(Optional.of(active))
        .thenReturn(Optional.empty());

    StudentBadge removed = subject.revokeActiveBadgeOfStudent(student.getId());

    assertTrue(removed.isRevoked());
    // nothing left to remove: the next print creates a new badge
    assertThrows(NotFoundException.class, () -> subject.getActiveBadgeOfStudent(student.getId()));
  }

  private static User student() {
    User student = new User();
    student.setId("student1_id");
    student.setRef("STD26001");
    return student;
  }

  private static StudentBadge badge(User student) {
    return StudentBadge.builder()
        .student(student)
        .publicId(PUBLIC_ID)
        .creationDatetime(Instant.parse("2026-10-01T00:00:00Z"))
        .build();
  }
}
