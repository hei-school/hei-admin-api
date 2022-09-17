package school.hei.haapi.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.EventParticipant;

import java.util.List;

@Repository
public interface EventParticipantRepository extends JpaRepository<EventParticipant, String> {

    EventParticipant getByUserParticipant_IdAndEvent_Id(String userParticipantId, String eventId);

    EventParticipant getByEvent_IdAndUserParticipant_KeyImageInBucket(String event_id, String userParticipant_keyImageInBucket);

    EventParticipant getByIdAndEvent_Id(String id, String eventId);

    List<EventParticipant> getAllByEvent_Id(String eventId);

    List<EventParticipant> getAllByEvent_Id(String eventId, Pageable pageable);

    List<EventParticipant> getAllByEvent_IdAndStatus(String eventId, school.hei.haapi.endpoint.rest.model.EventParticipant.StatusEnum status);

    List<EventParticipant> getAllByEvent_IdAndStatus(String eventId, school.hei.haapi.endpoint.rest.model.EventParticipant.StatusEnum status, Pageable pageable);

    List<EventParticipant> getAllByStatus(school.hei.haapi.endpoint.rest.model.EventParticipant.StatusEnum status);

    List<EventParticipant> getAllByStatus(school.hei.haapi.endpoint.rest.model.EventParticipant.StatusEnum status, Pageable pageable);

    void deleteAllByEvent_Id(String eventId);
}
