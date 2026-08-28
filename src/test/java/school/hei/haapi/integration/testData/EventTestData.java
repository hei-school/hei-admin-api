package school.hei.haapi.integration.testData;

import static java.util.UUID.randomUUID;
import static school.hei.haapi.model.Event.PlaceName.IVANDRY;
import static school.hei.haapi.model.Event.RoomName.UNKNOWN;

import java.time.Instant;
import java.util.ArrayList;
import school.hei.haapi.endpoint.rest.model.AttendanceStatus;
import school.hei.haapi.endpoint.rest.model.EventType;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.User;

public class EventTestData {
  public static Event anEvent(
      User planner, EventType type, String title, Instant begin, Instant end) {
    return Event.builder()
        .id(randomUUID().toString())
        .type(type)
        .title(title)
        .description(title + " — description")
        .colorCode("#0000")
        .room(UNKNOWN)
        .place(IVANDRY)
        .isDeleted(false)
        .isOnline(false)
        .beginDatetime(begin)
        .endDatetime(end)
        .planner(planner)
        .groups(new ArrayList<>())
        .build();
  }

  public static EventParticipant aParticipant(
      Event event, User participant, Group group, AttendanceStatus status) {
    return EventParticipant.builder()
        .id(randomUUID().toString())
        .event(event)
        .participant(participant)
        .group(group)
        .status(status)
        .letters(new ArrayList<>())
        .build();
  }
}
