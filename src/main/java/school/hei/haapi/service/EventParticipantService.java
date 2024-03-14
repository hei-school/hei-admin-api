package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.repository.EventParticipantRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class EventParticipantService {

    private final EventParticipantRepository eventParticipantRepository;

    public List<EventParticipant> getEventParticipants(Event event){
        return eventParticipantRepository.findAllByEvent(event);
    }

}
