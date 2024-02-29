package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.repository.EventRepository;

@Service
@AllArgsConstructor
public class EventService {

    private final EventRepository eventRepository;



}
