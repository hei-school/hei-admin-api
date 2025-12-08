package school.hei.haapi.service;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.util.Comparator.comparing;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;
import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.MISSING;
import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.PRESENT;
import static school.hei.haapi.endpoint.rest.model.EnableStatus.DISABLED;
import static school.hei.haapi.endpoint.rest.model.EventType.COURSE;
import static school.hei.haapi.endpoint.rest.model.FrequencyScopeDay.MONDAY;
import static school.hei.haapi.integration.StudentIT.someCreatableStudentList;
import static school.hei.haapi.integration.conf.FakeDataProvider.someCreatableEvent;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER_ID;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpEventBridge;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.mapper.EventMapper;
import school.hei.haapi.endpoint.rest.mapper.GroupMapper;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.endpoint.rest.model.EventParticipantStats;
import school.hei.haapi.endpoint.rest.model.EventStats;
import school.hei.haapi.endpoint.rest.model.MissedEventStats;
import school.hei.haapi.http.model.CreateEventFrequency;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.FakeDataProvider;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.EventFrequencyNumber;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.model.notEntity.CreateGroup;
import school.hei.haapi.service.utils.InstantUtils;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

@Testcontainers
@AutoConfigureMockMvc
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
class DirtyEventServiceTest extends FacadeITMockedThirdParties {
  @Autowired private EventService subject;
  @Autowired private EventMapper eventMapper;
  @Autowired private EventParticipantService participantService;
  @Autowired private UserService userService;
  @Autowired private GroupService groupService;
  @Autowired private GroupMapper groupMapper;
  @Autowired private UserMapper userMapper;
  @MockBean private EventBridgeClient eventBridgeClientMock;
  @Autowired private FakeDataProvider fakeDataProvider;

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
    setUpEventBridge(eventBridgeClientMock);
  }

  @Test
  void create_event_no_disabled_student_ok() {
    var disabledStudents =
        userService.saveAll(
            someCreatableStudentList(1).stream()
                .map(
                    user -> {
                      user.status(DISABLED);
                      return userMapper.toDomain(user);
                    })
                .toList());
    var randomGroup =
        groupService.saveAll(
            List.of(
                new CreateGroup(
                    groupMapper.toDomain(fakeDataProvider.createGroup()),
                    disabledStudents.stream().map(User::getId).toList())));
    var creatableEvent =
        eventMapper.toDomain(
            someCreatableEvent(
                COURSE,
                MANAGER_ID,
                now(),
                now().plusSeconds(69),
                randomGroup.stream().map(groupMapper::toRest).toList()));
    List<Event> createdEvents =
        subject.createOrUpdateEvent(
            List.of(creatableEvent), CreateEventFrequency.builder().build());

    var eventParticipants =
        participantService.getEventParticipants(
            createdEvents.getFirst().getId(),
            new PageFromOne(1),
            new BoundedPageSize(10),
            null,
            null,
            null,
            null,
            null);

    assertTrue(eventParticipants.isEmpty());
  }

  @Test
  void create_event_trigger_event_participant_creation() {
    List<User> randomUsers =
        userService.saveAll(
            someCreatableStudentList(1).stream().map(userMapper::toDomain).toList());
    List<Group> randomGroups =
        groupService.saveAll(
            List.of(
                new CreateGroup(
                    groupMapper.toDomain(fakeDataProvider.createGroup()),
                    randomUsers.stream().map(User::getId).toList())));

    Event creatableEvent =
        eventMapper.toDomain(
            someCreatableEvent(
                COURSE,
                MANAGER_ID,
                now(),
                now().plusSeconds(60),
                randomGroups.stream().map(groupMapper::toRest).toList()));
    List<Event> randomCourseEvent =
        subject.createOrUpdateEvent(
            List.of(creatableEvent), CreateEventFrequency.builder().build());

    List<EventParticipant> eventParticipants =
        participantService.getEventParticipants(
            randomCourseEvent.getFirst().getId(),
            new PageFromOne(1),
            new BoundedPageSize(10),
            null,
            null,
            null,
            null,
            null);

    assertEquals(randomUsers.getFirst(), eventParticipants.getFirst().getParticipant());

    // Assert that participant is not duplicated
    subject.createOrUpdateEvent(List.of(creatableEvent), CreateEventFrequency.builder().build());

    assertEquals(1, eventParticipants.size());
  }

  @Test
  void create_event_by_frequency_ok() {
    Instant eventBeginDate = InstantUtils.mondayOfTheWeek(LocalDate.of(2023, 12, 8));
    Event creatableEvent =
        eventMapper.toDomain(
            someCreatableEvent(
                COURSE, MANAGER_ID, eventBeginDate, eventBeginDate.plusSeconds(60), List.of()));
    List<Event> createdEvent =
        subject.createOrUpdateEvent(
            List.of(creatableEvent),
            CreateEventFrequency.builder()
                .frequencyScopeDay(MONDAY)
                .eventFrequencyNumber(new EventFrequencyNumber(3))
                .frequencyBeginningHour("10:00")
                .frequencyEndingHour("11:00")
                .build());

    // The count of the event must match
    assertEquals(4, createdEvent.size());

    // Sort the result for better readability in the test
    List<Event> sortedEvent =
        createdEvent.stream().sorted(comparing(Event::getBeginDatetime)).toList();

    // The events are separated by 1 week
    Event eventWeek1 = sortedEvent.getFirst();
    assertEquals(creatableEvent.getBeginDatetime(), eventWeek1.getBeginDatetime());
    assertEquals(creatableEvent.getEndDatetime(), eventWeek1.getEndDatetime());

    Event eventWeek2 = sortedEvent.get(1);
    assertEquals(Instant.parse("2023-12-11T10:00:00+03:00"), eventWeek2.getBeginDatetime());
    assertEquals(Instant.parse("2023-12-11T11:00:00+03:00"), eventWeek2.getEndDatetime());

    Event eventWeek3 = sortedEvent.get(2);
    assertEquals(Instant.parse("2023-12-18T10:00:00+03:00"), eventWeek3.getBeginDatetime());
    assertEquals(Instant.parse("2023-12-18T11:00:00+03:00"), eventWeek3.getEndDatetime());

    Event eventWeek4 = sortedEvent.get(3);
    assertEquals(Instant.parse("2023-12-25T10:00:00+03:00"), eventWeek4.getBeginDatetime());
    assertEquals(Instant.parse("2023-12-25T11:00:00+03:00"), eventWeek4.getEndDatetime());
  }

  @Test
  void event_stats_are_exact() {
    List<User> randomUsers =
        userService.saveAll(
            someCreatableStudentList(1).stream().map(userMapper::toDomain).toList());
    List<Group> randomGroups =
        groupService.saveAll(
            List.of(
                new CreateGroup(
                    groupMapper.toDomain(fakeDataProvider.createGroup()),
                    randomUsers.stream().map(User::getId).toList())));

    List<Event> randomCourseEvent =
        subject.createOrUpdateEvent(
            List.of(
                eventMapper.toDomain(
                    someCreatableEvent(
                        COURSE,
                        MANAGER_ID,
                        now(),
                        now().plusSeconds(60),
                        randomGroups.stream().map(groupMapper::toRest).toList()))),
            CreateEventFrequency.builder().build());

    List<EventParticipant> eventParticipants =
        participantService.getEventParticipants(
            randomCourseEvent.getFirst().getId(),
            new PageFromOne(1),
            new BoundedPageSize(10),
            null,
            null,
            null,
            null,
            null);

    eventParticipants.getFirst().setStatus(PRESENT);
    participantService.updateEventParticipants(eventParticipants);

    EventStats stats = subject.getStats(randomCourseEvent.getFirst().getId(), null, null);
    var expectedStats =
        new EventStats()
            .late(0L)
            .missedStats(new MissedEventStats().justified(0L).unjustified(0L).total(0L))
            .unchecked(0L)
            .total(1L)
            .present(1L);
    assertEquals(expectedStats, stats);
  }

  @Test
  void event_participant_stats_are_exact() {
    List<User> randomUsers =
        userService.saveAll(
            someCreatableStudentList(1).stream().map(userMapper::toDomain).toList());
    List<Group> randomGroups =
        groupService.saveAll(
            List.of(
                new CreateGroup(
                    groupMapper.toDomain(fakeDataProvider.createGroup()),
                    randomUsers.stream().map(User::getId).toList())));
    var randomCourseEvent =
        subject.createOrUpdateEvent(
            List.of(
                eventMapper.toDomain(
                    someCreatableEvent(
                        COURSE,
                        MANAGER_ID,
                        now(),
                        now().plusSeconds(60),
                        randomGroups.stream().map(groupMapper::toRest).toList()))),
            CreateEventFrequency.builder().build());
    List<EventParticipant> eventParticipants =
        participantService.getEventParticipants(
            randomCourseEvent.getFirst().getId(),
            new PageFromOne(1),
            new BoundedPageSize(10),
            null,
            null,
            null,
            null,
            null);

    eventParticipants.getFirst().setStatus(MISSING);
    participantService.updateEventParticipants(eventParticipants);

    var randomStudentStats =
        participantService.getStudentEventStats(
            randomUsers.getFirst().getId(), now().minus(1, DAYS), now().plus(1, HOURS));
    var expectedStats =
        new EventParticipantStats()
            .missedEvents(new MissedEventStats().justified(0L).unjustified(1L).total(1L))
            .assistedEvents(0L)
            .lateEvents(0L)
            .uncheckedEvents(0L)
            .totalEvents(1L);
    assertEquals(expectedStats, randomStudentStats);
  }
}
