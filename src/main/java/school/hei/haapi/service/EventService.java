package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.repository.EventRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EventService {
    private final EventRepository eventRepository;

    public List<Event> getAllOrByPlaceId(PageFromOne page, BoundedPageSize pageSize, String placeId) {
        Pageable pageable = PageRequest.of(page.getValue() - 1, pageSize.getValue(),Sort.by(Sort.Direction.DESC,"starting_date_time"));
        if (placeId != null) {
            return eventRepository.findEventFromPlace(pageable,placeId);
        }
        return new ArrayList<>(eventRepository.findAll());
    }

    public List<Event> createOrUpdate(List<Event> events){
        return eventRepository.saveAll(events);
    }

}
