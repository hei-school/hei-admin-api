package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.model.validator.EventParticipantValidator;
import school.hei.haapi.repository.EventParticipantRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@AllArgsConstructor
public class EventParticipantService {
    private final EventParticipantValidator eventParticipantValidator;
    private final EventParticipantRepository eventParticipantRepository;

    public EventParticipant getById(String eventParticipantId){
        return eventParticipantRepository.getById(eventParticipantId);
    }

    public List<EventParticipant> getAll(){
        return eventParticipantRepository.findAll();
    }

    @Transactional
    public List<EventParticipant> saveAll(List<EventParticipant> eventParticipants){
        eventParticipantValidator.accept(eventParticipants);
        return eventParticipantRepository.saveAll(eventParticipants);
    }
}
