package school.hei.haapi.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.haapi.model.EventParticipant;

import java.util.List;

public interface EventParticipantRepository extends JpaRepository<EventParticipant, String> {
  List<EventParticipant> findByEvent_IdContainingIgnoreCaseAndIdContainingIgnoreCaseAndStatus(String eventId, String ref, EventParticipant.StatusEnum status, Pageable pageable);
}
