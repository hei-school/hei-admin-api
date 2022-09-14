package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.haapi.model.EventParticipant;

public interface EventParticipantRepository extends JpaRepository<EventParticipant, String> {
}
