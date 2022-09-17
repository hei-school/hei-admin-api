package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.validator.EventParticipantValidator;
import school.hei.haapi.repository.EventParticipantRepository;
import school.hei.haapi.repository.EventRepository;
import school.hei.haapi.repository.UserRepository;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.ASC;

@Service
@AllArgsConstructor
public class EventParticipantService {
    private final EventParticipantValidator eventParticipantValidator;
    private final EventParticipantRepository eventParticipantRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public EventParticipant getById(String eventId, String eventParticipantId){
        return eventParticipantRepository.findEventParticipantByEvent_IdAndId(eventId,eventParticipantId);
    }

    public List<EventParticipant> getEventParticipantsByEventId(
            String eventId,
            PageFromOne page,
            BoundedPageSize pageSize
    ){
        Pageable pageable = PageRequest.of(
                page.getValue() - 1,
                pageSize.getValue(),
                Sort.by(ASC, "status"));
        return eventParticipantRepository.getByEventId(eventId, pageable);
    }
    public List<EventParticipant> getEventParticipantsByEventId(
            String eventId
    ){
        return eventParticipantRepository.getByEventId(eventId);
    }

    public List<EventParticipant> getAll(
            PageFromOne page,
            BoundedPageSize pageSize
    ){
        Pageable pageable = PageRequest.of(
                page.getValue() - 1,
                pageSize.getValue(),
                Sort.by(ASC, "status"));
        return eventParticipantRepository.findAll(pageable).toList();
    }

    public List<User> getParticipantsByEventId(
            String eventId
    ){
        List<User> listParticipant = new ArrayList<>();
        List<EventParticipant> list = getEventParticipantsByEventId(eventId);
        for (EventParticipant p:list) {
            listParticipant.add(p.getParticipant());
        }
        return listParticipant;
    }



    @Transactional
    public EventParticipant accept(EventParticipant eventParticipant){
        eventParticipantValidator.accept(eventParticipant);
        Event event = eventRepository
                .findById(eventParticipant.getEvent().getId())
                .orElseThrow(() ->
                        new BadRequestException("Event with id "+eventParticipant
                                .getEvent()
                                .getId()+"doesn't exist"));
        User user = userRepository
                .findById(eventParticipant.getParticipant().getId())
                .orElseThrow(() ->
                        new BadRequestException("User with id "+eventParticipant
                                .getParticipant()
                                .getId()+"doesn't exist"));
        eventParticipant.setEvent(event);
        eventParticipant.setParticipant(user);
        return eventParticipant;
    }

    @Transactional
    public List<EventParticipant> saveAll(List<EventParticipant> eventParticipants){
        List<EventParticipant> saved = new ArrayList<>();
        eventParticipants
                .forEach(eventParticipant ->
                        saved.add(accept(eventParticipant)));
        return eventParticipantRepository.saveAll(saved);
    }
}
