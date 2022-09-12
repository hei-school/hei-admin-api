package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Event;
import school.hei.haapi.repository.EventRepository;

@Service
@AllArgsConstructor
public class EventService {

    private final EventRepository repository;

    public Event getById(String eventId) {
        return repository.getById(eventId);
    }

    public List<Event> getAll() {
        return repository.findAll();
    }

    public List<Event> saveAll(List<Event> events) {
        return repository.saveAll(events);
    }
}
