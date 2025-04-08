package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.EventAttendance;
import school.hei.haapi.model.EventParticipant;

@Component
@AllArgsConstructor
public class EventAttendanceMapper {
  private final EventParticipantMapper eventParticipantMapper;
  private final EventMapper eventMapper;

  public EventAttendance toRest(EventParticipant eventParticipant) {
    return new EventAttendance()
        .eventParticipant(eventParticipantMapper.toRest(eventParticipant))
        .event(eventMapper.toRest(eventParticipant.getEvent()));
  }
}
