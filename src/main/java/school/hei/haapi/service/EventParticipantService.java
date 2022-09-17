package school.hei.haapi.service;

import com.amazonaws.services.rekognition.model.CompareFacesMatch;
import java.util.List;
import javax.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.repository.EventParticipantRepository;
import school.hei.haapi.service.AWS.RekognitionService;

@Service
@AllArgsConstructor
public class EventParticipantService {
  private final EventParticipantRepository repository;
  private final RekognitionService rekognitionService;

  public List<EventParticipant> getAll(PageFromOne page,
                                       BoundedPageSize pageSize, String eventId, String ref,
                                       String status) {
    Pageable pageable = PageRequest.of(page.getValue() - 1, pageSize.getValue());
    return repository.findByEvent_IdContainingIgnoreCaseAndIdContainingIgnoreCaseAndStatus(eventId,
        ref,
        EventParticipant.StatusEnum.valueOf(status), pageable);
  }

  public EventParticipant getById(String eventId, String participantId) {
    return repository.getById(participantId);
  }

  @Transactional
  public List<EventParticipant> saveAll(List<EventParticipant> toCreate) {
    return repository.saveAll(toCreate);
  }


  @Transactional
  public String checkAttendance(String event_id, byte[] toCompare) {
    List<EventParticipant> participants =
        getAll(new PageFromOne(1), new BoundedPageSize(500), event_id, "",
            EventParticipant.StatusEnum.EXPECTED.name());
    int present = 0;
    for (EventParticipant participant : participants) {
      List<CompareFacesMatch> result =
          rekognitionService.compareFaces(toCompare, participant.getUser().getPicture())
              .getFaceMatches();
      for (CompareFacesMatch match : result) {
        if (match.getSimilarity() >=
            RekognitionService.SIMILARITY_THRESHOLD) {
            present++;
          participant.setStatus(EventParticipant.StatusEnum.HERE);
        }
      }
    }
    return "Attendance Result : number of participants " + participants.size() + " present : " +
        present;
  }
}
