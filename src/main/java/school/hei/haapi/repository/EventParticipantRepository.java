package school.hei.haapi.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.haapi.model.EventParticipant;

import java.util.List;

public interface EventParticipantRepository extends JpaRepository<EventParticipant, String> {
    EventParticipant findEventParticipantByEvent_IdAndId(String eventId, String Id);
    List<EventParticipant> findAllByEvent_Id(String eventId, Pageable pageable);
}
