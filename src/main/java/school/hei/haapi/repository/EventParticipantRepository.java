package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.EventParticipant;

@Repository
public interface EventParticipantRepository extends JpaRepository<EventParticipant, String> {

  List<EventParticipant> findAllByEventId(String eventId, Pageable pageable);

  List<EventParticipant> findAllByEventIdAndGroupRef(
      String eventId, String groupRef, Pageable pageable);
}
