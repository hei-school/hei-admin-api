package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.validator.EventValidator;
import school.hei.haapi.repository.EventRepository;

import javax.transaction.Transactional;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.ASC;

@Service
@AllArgsConstructor
public class EventService {
    private final EventValidator eventValidator;
    private final EventRepository eventRepository;

    public Event getById(String eventId){
        return eventRepository.getById(eventId);
    }

    public List<Event> getAll(PageFromOne page, BoundedPageSize pageSize, String name){
        Pageable pageable = PageRequest.of(
                page.getValue() - 1,
                pageSize.getValue(),
                Sort.by(ASC, "startEvent"));
        return eventRepository.findByNameContainingIgnoreCase(pageable,name);
    }

    @Transactional
    public List<Event> saveAll(List<Event> events){
        eventValidator.accept(events);
        return eventRepository.saveAll(events);
    }

    public List<Event> getEventsByResponsibleId(String id_responsible,
                                                PageFromOne page, BoundedPageSize pageSize) {
        Pageable pageable = PageRequest.of(
                page.getValue() - 1,
                pageSize.getValue(),
                Sort.by(ASC, "startEvent"));
        return eventRepository.findByResponsibleId(id_responsible,pageable);
    }
}
