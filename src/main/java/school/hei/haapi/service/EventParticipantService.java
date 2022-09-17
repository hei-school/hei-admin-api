package school.hei.haapi.service;

import com.amazonaws.services.rekognition.model.CompareFacesResult;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.repository.EventParticipantRepository;
import school.hei.haapi.repository.EventRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class EventParticipantService {
  private final RekognitionService rekognitionService;

  private final EventParticipantRepository eventParticipantRepository;

  public List<EventParticipant> getAll (
      PageFromOne page, BoundedPageSize pageSize, String eventId,String ref,String status
  ){
    Pageable pageable = PageRequest.of(page.getValue() -1 , pageSize.getValue());
    return eventParticipantRepository.findByEvent_IdContainingIgnoreCaseAndIdContainingIgnoreCaseAndStatus(
        eventId,ref, EventParticipant.StatusEnum.valueOf(status), pageable
    );
  }

  public EventParticipant getByIdAndEvent(String ref, String event){
    return eventParticipantRepository.getByUser_RefAndEvent_Id(ref,event);
  }

  @Transactional
  public List<EventParticipant> saveAll (List<EventParticipant> toSave){
    return eventParticipantRepository.saveAll(toSave);
  }

  public String performAttendance(byte[] image, String ref, String event){
    CompareFacesResult result = rekognitionService.compareFaces(image,ref);
    if(result.getFaceMatches().size() == 1){
      EventParticipant actual = this.getByIdAndEvent(ref, event);
      actual.setStatus(EventParticipant.StatusEnum.HERE);
      eventParticipantRepository.save(actual);
      return "User having reference : " + ref + "checked as present";
    }
    else {
      EventParticipant actual = this.getByIdAndEvent(ref, event);
      actual.setStatus(EventParticipant.StatusEnum.MISSING);
      eventParticipantRepository.save(actual);
      return "User having reference : " + ref + "checked as missing";
    }
  }
}
