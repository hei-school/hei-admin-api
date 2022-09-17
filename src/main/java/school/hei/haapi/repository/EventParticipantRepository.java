package school.hei.haapi.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.EventParticipant;
import java.util.List;

@Repository
public interface EventParticipantRepository extends JpaRepository<EventParticipant, String> {
    @Query("select e from EventParticipant e where e.event.id = ?1 and e.event.eventType = ?2 order by e.user.firstName,e.user.lastName")
    List<EventParticipant> getEventParticipantForEvent(String eventId, String eventType,Pageable pageable);
    List<EventParticipant> findEventParticipantByEventIdOrEventEventType(String eventId, String eventType,Pageable pageable);
}