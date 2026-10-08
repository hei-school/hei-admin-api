package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.PRESENT;
import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.UNCHECKED;
import static school.hei.haapi.endpoint.rest.model.BadgeAttendanceResult.CHECKED;
import static school.hei.haapi.endpoint.rest.model.BadgeAttendanceResult.NOT_PARTICIPANT;
import static school.hei.haapi.endpoint.rest.model.BadgeAttendanceResult.NO_COURSE_IN_PROGRESS;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.model.StudentBadge;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.repository.EventParticipantRepository;
import school.hei.haapi.repository.dao.EventDao;

class BadgeAttendanceServiceTest {
  private static final String PUBLIC_ID = "7c1e4b9a-2f3d-4e8a-9b6c-1d2e3f4a5b6c";
  private StudentBadgeCodeService studentBadgeCodeService;
  private EventDao eventDao;
  private EventParticipantRepository eventParticipantRepository;
  private BadgeAttendanceService subject;
  private final User teacher = user("teacher1_id");
  private final User student = user("student1_id");

  @BeforeEach
  void setUp() {
    studentBadgeCodeService = mock(StudentBadgeCodeService.class);
    eventDao = mock(EventDao.class);
    eventParticipantRepository = mock(EventParticipantRepository.class);
    subject =
        new BadgeAttendanceService(studentBadgeCodeService, eventDao, eventParticipantRepository);
    when(studentBadgeCodeService.getValidBadge(PUBLIC_ID))
        .thenReturn(StudentBadge.builder().student(student).publicId(PUBLIC_ID).build());
  }

  @Test
  void student_is_marked_present_to_the_course_in_progress_of_the_teacher() {
    var prog = course("prog_id", "PROG1", Duration.ofMinutes(-30), Duration.ofMinutes(60));
    var participant = EventParticipant.builder().participant(student).status(UNCHECKED).build();
    givenCoursesOfTeacher(prog);
    when(eventParticipantRepository.findEventParticipantByParticipantIdAndEventId(
            "student1_id", "prog_id"))
        .thenReturn(Optional.of(participant));

    var attendance = subject.checkAttendanceToCourseOf(teacher, PUBLIC_ID);

    assertEquals(CHECKED, attendance.getResult());
    assertEquals("PROG1", attendance.getCourseCode());
    assertEquals(PRESENT, participant.getStatus());
    verify(eventParticipantRepository).save(participant);
  }

  @Test
  void course_of_the_student_is_found_among_the_courses_in_progress() {
    var web = course("web_id", "WEB1", Duration.ofMinutes(-10), Duration.ofMinutes(80));
    var prog = course("prog_id", "PROG1", Duration.ofMinutes(-60), Duration.ofMinutes(30));
    var participant = EventParticipant.builder().participant(student).status(UNCHECKED).build();
    givenCoursesOfTeacher(prog, web);
    when(eventParticipantRepository.findEventParticipantByParticipantIdAndEventId(
            "student1_id", "prog_id"))
        .thenReturn(Optional.of(participant));

    assertEquals("PROG1", subject.checkAttendanceToCourseOf(teacher, PUBLIC_ID).getCourseCode());
  }

  @Test
  void nothing_is_marked_without_course_in_progress() {
    var finished = course("past_id", "PROG1", Duration.ofMinutes(-120), Duration.ofMinutes(-1));
    givenCoursesOfTeacher(finished);

    assertEquals(
        NO_COURSE_IN_PROGRESS, subject.checkAttendanceToCourseOf(teacher, PUBLIC_ID).getResult());
    verify(eventParticipantRepository, never()).save(any());
  }

  @Test
  void nothing_is_marked_when_the_student_does_not_take_part_in_the_course() {
    givenCoursesOfTeacher(
        course("prog_id", "PROG1", Duration.ofMinutes(-30), Duration.ofMinutes(60)));
    when(eventParticipantRepository.findEventParticipantByParticipantIdAndEventId(
            "student1_id", "prog_id"))
        .thenReturn(Optional.empty());

    var attendance = subject.checkAttendanceToCourseOf(teacher, PUBLIC_ID);

    assertEquals(NOT_PARTICIPANT, attendance.getResult());
    assertEquals("PROG1", attendance.getCourseCode());
    verify(eventParticipantRepository, never()).save(any());
  }

  @Test
  void invalid_badge_marks_nothing() {
    when(studentBadgeCodeService.getValidBadge(PUBLIC_ID))
        .thenThrow(new BadRequestException("The badge has been revoked"));

    assertThrows(
        BadRequestException.class, () -> subject.checkAttendanceToCourseOf(teacher, PUBLIC_ID));
  }

  private void givenCoursesOfTeacher(Event... events) {
    when(eventDao.findByCriteria(
            isNull(), any(), any(), isNull(), isNull(), isNull(), eq("teacher1_id"), isNull()))
        .thenReturn(List.of(events));
  }

  private static Event course(String id, String code, Duration begin, Duration end) {
    var now = Instant.now();
    return Event.builder()
        .id(id)
        .title("Cours " + code)
        .course(Course.builder().code(code).build())
        .beginDatetime(now.plus(begin))
        .endDatetime(now.plus(end))
        .build();
  }

  private static User user(String id) {
    var user = new User();
    user.setId(id);
    return user;
  }
}
