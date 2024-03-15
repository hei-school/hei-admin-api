package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.EventParticipantRepository;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
@AllArgsConstructor
public class EventParticipantService {

    private final EventParticipantRepository eventParticipantRepository;
    private final UserService userService;

    public List<EventParticipant> getEventParticipants(String eventId, PageFromOne page, BoundedPageSize pageSize, String groupId){
         List<EventParticipant> eventParticipantsByGroup = new ArrayList<>();

         List<User> users = userService.getByGroupId(groupId);

         for(User user : users){
             eventParticipantsByGroup.add(eventParticipantRepository.findByParticipantEmail(user.getEmail()));
         }

         Pageable pageable =
                PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "id"));
         List<EventParticipant> eventParticipants = eventParticipantRepository.findAllByEventId(eventId, pageable);


         return eventParticipants;
    }

    public List<EventParticipant> crupdateEventParticipants(List<EventParticipant> eventParticipants){
        return eventParticipantRepository.saveAll(eventParticipants);
    }

    public EventParticipant getByEmail(String email){
        return eventParticipantRepository.findByParticipantEmail(email);
    }

}
