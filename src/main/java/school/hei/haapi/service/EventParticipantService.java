package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.validator.EventParticipantValidator;
import school.hei.haapi.repository.EventParticipantRepository;
import school.hei.haapi.repository.EventRepository;
import school.hei.haapi.repository.UserRepository;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class EventParticipantService {
    private final EventParticipantValidator eventParticipantValidator;
    private final EventParticipantRepository eventParticipantRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public EventParticipant getById(String eventParticipantId){
        return eventParticipantRepository.getById(eventParticipantId);
    }

    public List<EventParticipant> getAll(){
        return eventParticipantRepository.findAll();
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
