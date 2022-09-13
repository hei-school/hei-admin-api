package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.validator.EventValidator;
import school.hei.haapi.repository.EventRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@AllArgsConstructor
public class EventService {
    private final EventValidator eventValidator;
    private final EventRepository eventRepository;

    public Event getById(String eventId){
        return eventRepository.getById(eventId);
    }

    public List<Event> getAll(){
        return eventRepository.findAll();
    }

    @Transactional
    public List<Event> saveAll(List<Event> events){
        eventValidator.accept(events);
        return eventRepository.saveAll(events);
    }
}
