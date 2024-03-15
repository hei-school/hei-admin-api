package school.hei.haapi.service;

import static org.springframework.data.domain.Sort.Direction.DESC;

import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.repository.EventParticipantRepository;

@Service
@AllArgsConstructor
public class EventParticipantService {

  private final EventParticipantRepository eventParticipantRepository;
  private final UserService userService;

  public List<EventParticipant> getEventParticipants(
      String eventId, PageFromOne page, BoundedPageSize pageSize, String groupId) {

    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "id"));

    return Objects.isNull(groupId)
        ? eventParticipantRepository.findAllByEventId(eventId, pageable)
        : eventParticipantRepository.findAllByEventIdAndGroupId(eventId, groupId, pageable);
  }

  public List<EventParticipant> crupdateEventParticipants(
      List<EventParticipant> eventParticipants) {
    return eventParticipantRepository.saveAll(eventParticipants);
  }
}
