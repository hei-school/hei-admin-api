package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.validator.EventValidator;
import school.hei.haapi.repository.EventRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@AllArgsConstructor
public class EventService {
    private EventRepository eventRepository;
    private EventValidator eventValidator;

    @Transactional
    public List<Event> saveAll(List<Event> eventList) {
        eventValidator.accept(eventList);
        return eventRepository.saveAll(eventList);
    }

    public Event getById(String eventId) {
        return eventRepository.getById(eventId);
    }

    public List<Event> getAll(String placeId) {
        if (placeId != null) {
            return this.getByPlaceId(placeId);
        }
        return eventRepository.findAll(Sort.by("startingHours").descending());
    }

    public List<Event> getByPlaceId(String placeId) {
        Pageable pageable = PageRequest.of(
                0,
                eventRepository.findAll().size(),
                Sort.by("startingHours").descending()
        );
        return eventRepository.getByPlace_Id(placeId, pageable);
    }


}
