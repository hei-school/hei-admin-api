package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.CreateEventParticipant;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.repository.EventParticipantRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class EventParticipantService {
    private final EventParticipantRepository eventParticipantRepository;

    public List<EventParticipant> getsByEventIdOrAndType(Pageable pageable, String eventId, String eventType){
        if (eventId != null && eventType!=null) {
            return eventParticipantRepository.getEventParticipantForEvent(eventId,eventType,pageable);
        }
        if (eventId == null && eventType==null) {
            throw new BadRequestException("Pass at least one of event_id and event_type");
        }
        return eventParticipantRepository.findEventParticipantByEventIdOrEventEventType(eventId, eventType, pageable);
    }

    public List<EventParticipant> saveAll(List<EventParticipant> toCreates){
        toCreates.forEach(eventParticipant -> {
            if (eventParticipant.getStatus() == null) {
                eventParticipant.setStatus(EventParticipant.Status.EXPECTED);
            }
        });
        return eventParticipantRepository.saveAll(toCreates);
    }
}
