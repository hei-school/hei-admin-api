package school.hei.haapi.service;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.model.validator.EventParticipantValidator;
import school.hei.haapi.repository.EventParticipantRepository;

import javax.transaction.Transactional;
import java.nio.file.NoSuchFileException;
import java.util.List;

@Service
@AllArgsConstructor
public class EventParticipantService {
    private EventParticipantRepository eventParticipantRepository;
    private EventParticipantValidator eventParticipantValidator;
    private AwsRekognitionService awsRekognitionService;

    public List<EventParticipant> getAll(Integer page, Integer pageSize, school.hei.haapi.endpoint.rest.model.EventParticipant.StatusEnum status) {
        if (page != null && pageSize != null) {
            if (status != null) {
                return eventParticipantRepository
                        .getAllByStatus(status, PageRequest.of(page, pageSize));
            }
            return eventParticipantRepository.findAll(PageRequest.of(page, pageSize)).toList();
        }
        if (status != null) {
            return eventParticipantRepository.getAllByStatus(status);
        }
        return eventParticipantRepository.findAll();
    }

    public EventParticipant updateStatusWithImage(String eventId, byte[] sourceImage) throws NoSuchFileException {
        S3ObjectSummary s3ObjectSummary = awsRekognitionService.compareFacesMatches(sourceImage);
        EventParticipant eventParticipant = eventParticipantRepository
                .getByEvent_IdAndUserParticipant_KeyImageInBucket(
                        eventId,
                        s3ObjectSummary.getKey()
                );
        eventParticipant.setStatus(school.hei.haapi.endpoint.rest.model.EventParticipant.StatusEnum.HERE);
        return eventParticipantRepository.save(eventParticipant);
    }

    public List<EventParticipant> getAllByEventId(Integer page, Integer pageSize, String eventId, school.hei.haapi.endpoint.rest.model.EventParticipant.StatusEnum status) {
        if (page != null && pageSize != null) {
            if (status != null) {
                return eventParticipantRepository
                        .getAllByEvent_IdAndStatus(eventId, status, PageRequest.of(page, pageSize));
            }
            return eventParticipantRepository
                    .getAllByEvent_Id(eventId, PageRequest.of(page, pageSize));
        }
        if (status != null) {
            return eventParticipantRepository
                    .getAllByEvent_IdAndStatus(eventId, status);
        }
        return eventParticipantRepository
                .getAllByEvent_Id(eventId);
    }

    public EventParticipant getByEventIdAndId(String eventId, String id) {
        return eventParticipantRepository.getByIdAndEvent_Id(id, eventId);
    }

    public EventParticipant getById(String id) {
        return eventParticipantRepository.getById(id);
    }

    @Transactional
    public List<EventParticipant> createAll(List<EventParticipant> eventParticipantList) {
        eventParticipantValidator.accept(eventParticipantList);
        return eventParticipantRepository.saveAll(eventParticipantList);
    }

    @Transactional
    public List<EventParticipant> updateAll(String eventId, List<EventParticipant> eventParticipantList) {
        for (EventParticipant eventParticipant : eventParticipantList) {
            eventParticipantRepository.getByIdAndEvent_Id(eventParticipant.getId(), eventId);
        }
        return eventParticipantRepository.saveAll(eventParticipantList);
    }
}
