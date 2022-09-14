package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.EventParticipant;

@Component
@AllArgsConstructor
public class EventParticipantMapper {
    public EventParticipant toRest(EventParticipant eventParticipant){
        var restEventParticipant = new EventParticipant();
        restEventParticipant.setId(eventParticipant.getId());
        restEventParticipant.setParticipant(eventParticipant.getParticipant());
        restEventParticipant.setStatus(eventParticipant.getStatus());
        return restEventParticipant;
    }

    public EventParticipant toDomain(EventParticipant restEventParticipant){
        return EventParticipant.builder()
                .id(restEventParticipant.getId())
                .participant(restEventParticipant.getParticipant())
                .status(restEventParticipant.getStatus())
                .build();
    }
}
