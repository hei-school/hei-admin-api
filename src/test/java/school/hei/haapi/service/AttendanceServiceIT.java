package school.hei.haapi.service;

import static java.time.LocalDate.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.MISSING;
import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.PRESENT;
import static school.hei.haapi.endpoint.rest.model.EventType.COURSE;
import static school.hei.haapi.model.Event.PlaceName.ANDRAHARO;
import static school.hei.haapi.model.Event.RoomName.ALGEBRE;
import static school.hei.haapi.model.User.Role.STUDENT;
import static school.hei.haapi.model.User.Status.ENABLED;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.model.StudentAttendance;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.EventParticipantRepository;
import school.hei.haapi.repository.EventRepository;
import school.hei.haapi.repository.UserRepository;

class AttendanceServiceIT extends FacadeITMockedThirdParties {
  @Autowired AttendanceService subject;
  @Autowired UserRepository userRepository;
  @Autowired EventParticipantRepository eventParticipantRepository;
  @Autowired EventRepository eventRepository;
  private User studentOne;
  private User studentTwo;
  private Event event;
  private EventParticipant eventParticipantOne;
  private EventParticipant eventParticipantTwo;
  private static final Instant startOfActualMonth =
      startOfActualMonth().atStartOfDay().toInstant(ZoneOffset.UTC);
  private static final Instant endOfActualMonth =
      endOfActualMonth().plusDays(1L).atStartOfDay().toInstant(ZoneOffset.UTC);
  private static final String EVENT_ID = randomUUID().toString();

  @BeforeEach
  void setUp() {
    event =
        eventRepository.save(
            Event.builder()
                .beginDatetime(missingStudentAttendanceStatus().beginDatetime())
                .endDatetime(missingStudentAttendanceStatus().endDatetime())
                .title(missingStudentAttendanceStatus().eventTitle())
                .description(missingStudentAttendanceStatus().eventDescription())
                .type(missingStudentAttendanceStatus().eventType())
                .room(missingStudentAttendanceStatus().room())
                .place(missingStudentAttendanceStatus().place())
                .build());
    studentOne =
        userRepository.save(
            User.builder()
                .ref(randomUUID().toString())
                .email(randomUUID() + "@email.com")
                .lastName("dummy")
                .firstName("dummy")
                .status(ENABLED)
                .entranceDatetime(Instant.now())
                .role(STUDENT)
                .build());
    studentTwo =
        userRepository.save(
            User.builder()
                .ref(randomUUID().toString())
                .email(randomUUID() + "@email.com")
                .lastName("dummy2")
                .firstName("dummy2")
                .status(ENABLED)
                .entranceDatetime(Instant.now())
                .role(STUDENT)
                .build());
    eventParticipantOne =
        eventParticipantRepository.save(
            EventParticipant.builder()
                .id(randomUUID().toString())
                .event(event)
                .participant(studentOne)
                .status(missingStudentAttendanceStatus().attendanceStatus())
                .build());
    eventParticipantTwo =
        eventParticipantRepository.save(
            EventParticipant.builder()
                .id(randomUUID().toString())
                .event(event)
                .participant(studentTwo)
                .status(presentStudentAttendanceStatus().attendanceStatus())
                .build());
  }

  @AfterEach
  void tearDown() {
    eventParticipantRepository.deleteAll(List.of(eventParticipantOne, eventParticipantTwo));
    eventRepository.delete(event);
    userRepository.deleteAll(List.of(studentOne, studentTwo));
  }

  @Test
  void get_student_missing_between_date() {
    var actual =
        subject.findStudentAttendanceByStudentId(
            studentOne.getId(), MISSING, startOfActualMonth, endOfActualMonth, List.of());
    var actualDefault =
        subject.findStudentAttendanceByStudentId(
            studentOne.getId(), null, startOfActualMonth, endOfActualMonth, List.of());

    assertEquals(actualDefault.getFirst().eventTitle(), actual.getFirst().eventTitle());
    assertEquals(actualDefault.getFirst().eventDescription(), actual.getFirst().eventDescription());
    assertEquals(actualDefault.getFirst().eventType(), actual.getFirst().eventType());
    assertEquals(actualDefault.getFirst().beginDatetime(), actual.getFirst().beginDatetime());
    assertEquals(actualDefault.getFirst().endDatetime(), actual.getFirst().endDatetime());
    assertEquals(actualDefault.getFirst().room(), actual.getFirst().room());
    assertEquals(actualDefault.getFirst().place(), actual.getFirst().place());

    assertEquals(missingStudentAttendanceStatus().eventTitle(), actual.getFirst().eventTitle());
    assertEquals(
        missingStudentAttendanceStatus().eventDescription(), actual.getFirst().eventDescription());
    assertEquals(missingStudentAttendanceStatus().eventType(), actual.getFirst().eventType());
    assertEquals(
        missingStudentAttendanceStatus().beginDatetime(), actual.getFirst().beginDatetime());
    assertEquals(missingStudentAttendanceStatus().endDatetime(), actual.getFirst().endDatetime());
    assertEquals(missingStudentAttendanceStatus().room(), actual.getFirst().room());
    assertEquals(missingStudentAttendanceStatus().place(), actual.getFirst().place());
  }

  @Test
  void get_student_present_between_date() {
    var actual =
        subject
            .findStudentAttendanceByStudentId(
                studentTwo.getId(), PRESENT, startOfActualMonth, endOfActualMonth, List.of())
            .getFirst();

    var studentAttendanceUpdated =
        new StudentAttendance(
            actual.eventId(),
            presentStudentAttendanceStatus().eventTitle(),
            presentStudentAttendanceStatus().eventDescription(),
            presentStudentAttendanceStatus().eventType(),
            presentStudentAttendanceStatus().attendanceStatus(),
            presentStudentAttendanceStatus().beginDatetime(),
            presentStudentAttendanceStatus().endDatetime(),
            presentStudentAttendanceStatus().room(),
            presentStudentAttendanceStatus().place());

    assertEquals(studentAttendanceUpdated, actual);
  }

  private static StudentAttendance missingStudentAttendanceStatus() {
    return new StudentAttendance(
        EVENT_ID,
        "eventTitle",
        "eventDescription",
        COURSE,
        MISSING,
        startOfActualMonth.plus(1L, DAYS),
        startOfActualMonth.plus(2L, DAYS),
        ALGEBRE,
        ANDRAHARO);
  }

  private static StudentAttendance presentStudentAttendanceStatus() {
    return new StudentAttendance(
        EVENT_ID,
        "eventTitle",
        "eventDescription",
        COURSE,
        PRESENT,
        startOfActualMonth.plus(1L, DAYS),
        startOfActualMonth.plus(2L, DAYS),
        ALGEBRE,
        ANDRAHARO);
  }

  private static LocalDate startOfActualMonth() {
    var today = now();
    var currentMonth = YearMonth.from(today);
    return currentMonth.atDay(1);
  }

  private static LocalDate endOfActualMonth() {
    var today = now();
    var currentMonth = YearMonth.from(today);
    return currentMonth.atEndOfMonth();
  }
}
