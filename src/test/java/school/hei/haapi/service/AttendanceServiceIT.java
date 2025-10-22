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

import java.time.*;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.model.StudentAttendanceStatus;
import school.hei.haapi.model.StudentAttendanceStatusRepresentation;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.EventParticipantRepository;
import school.hei.haapi.repository.EventRepository;
import school.hei.haapi.repository.UserRepository;

@Slf4j
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
  private final Instant startOfActualMonth =
      startOfActualMonth().atStartOfDay().toInstant(ZoneOffset.UTC);
  private final Instant endOfActualMonth =
      endOfActualMonth().plusDays(1L).atStartOfDay().toInstant(ZoneOffset.UTC);

  @BeforeEach
  void setUp() {
    event =
        eventRepository.save(
            Event.builder()
                .id(randomUUID().toString())
                .beginDatetime(missingStudentAttendanceStatus().getBeginDatetime())
                .endDatetime(missingStudentAttendanceStatus().getEndDatetime())
                .title(missingStudentAttendanceStatus().getEventTitle())
                .description(missingStudentAttendanceStatus().getEventDescription())
                .type(missingStudentAttendanceStatus().getEventType())
                .room(missingStudentAttendanceStatus().getRoom())
                .place(missingStudentAttendanceStatus().getPlace())
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
                .status(missingStudentAttendanceStatus().getAttendanceStatus())
                .build());
    eventParticipantTwo =
        eventParticipantRepository.save(
            EventParticipant.builder()
                .id(randomUUID().toString())
                .event(event)
                .participant(studentTwo)
                .status(presentStudentAttendanceStatus().getAttendanceStatus())
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
        subject.getStudentAttendanceByStudentId(
            studentOne.getId(), MISSING, startOfActualMonth, endOfActualMonth, List.of());
    var actualDefault =
        subject.getStudentAttendanceByStudentId(
            studentOne.getId(), null, startOfActualMonth, endOfActualMonth, List.of());

    assertEquals(actualDefault, actual);
    assertEquals(List.of(missingStudentAttendanceStatus()), actual);
  }

  @Test
  void get_student_present_between_date() {
    var actual =
        subject.getStudentAttendanceByStudentId(
            studentTwo.getId(), PRESENT, startOfActualMonth, endOfActualMonth, List.of());

    assertEquals(List.of(presentStudentAttendanceStatus()), actual);
  }

  private StudentAttendanceStatusRepresentation missingStudentAttendanceStatus() {
    return new StudentAttendanceStatusRepresentation(
        "eventTitle",
        "eventDescription",
        COURSE,
        MISSING,
        startOfActualMonth.plus(1L, DAYS),
        startOfActualMonth.plus(2L, DAYS),
        ALGEBRE,
        ANDRAHARO);
  }

  private StudentAttendanceStatusRepresentation presentStudentAttendanceStatus() {
    return new StudentAttendanceStatusRepresentation(
        "eventTitle",
        "eventDescription",
        COURSE,
        PRESENT,
        startOfActualMonth.plus(1L, DAYS),
        startOfActualMonth.plus(2L, DAYS),
        ALGEBRE,
        ANDRAHARO);
  }

  private LocalDate startOfActualMonth() {
    var today = now();
    var currentMonth = YearMonth.from(today);
    return currentMonth.atDay(1);
  }

  private LocalDate endOfActualMonth() {
    var today = now();
    var currentMonth = YearMonth.from(today);
    return currentMonth.atEndOfMonth();
  }
}
