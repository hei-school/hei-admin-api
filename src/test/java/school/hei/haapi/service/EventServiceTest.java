package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static school.hei.haapi.endpoint.rest.model.EventType.COURSE;
import static school.hei.haapi.integration.StudentIT.someCreatableStudentList;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER_ID;
import static school.hei.haapi.integration.conf.TestUtils.createGroup;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpEventBridge;
import static school.hei.haapi.integration.conf.TestUtils.someCreatableEvent;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.mapper.EventMapper;
import school.hei.haapi.endpoint.rest.mapper.GroupMapper;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.http.model.CreateEventFrequency;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.model.notEntity.CreateGroup;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

@Testcontainers
@AutoConfigureMockMvc
class EventServiceTest extends FacadeITMockedThirdParties {
  @Autowired private EventService subject;
  @Autowired private EventMapper eventMapper;
  @Autowired private EventParticipantService participantService;
  @Autowired private UserService userService;
  @Autowired private GroupService groupService;
  @Autowired private GroupMapper groupMapper;
  @Autowired private UserMapper userMapper;
  @MockBean private EventBridgeClient eventBridgeClientMock;

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
    setUpEventBridge(eventBridgeClientMock);
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
                    groupMapper.toDomain(createGroup()),
                    randomUsers.stream().map(User::getId).toList())));

    Event creatableEvent =
        eventMapper.toDomain(
            someCreatableEvent(
                COURSE,
                MANAGER_ID,
                Instant.now(),
                Instant.now().plusSeconds(60),
                randomGroups.stream().map(groupMapper::toRest).toList()));
    List<Event> createdEvent =
        subject.createOrUpdateEvent(
            List.of(creatableEvent), CreateEventFrequency.builder().build());

    List<EventParticipant> eventParticipants =
        participantService.getEventParticipants(
            createdEvent.getFirst().getId(),
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
}
