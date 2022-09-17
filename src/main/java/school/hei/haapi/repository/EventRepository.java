package school.hei.haapi.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Event;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {
    @Query("select e from Event e where e.place.id = ?1 order by e.startingDateTime DESC")
    List<Event> findEventFromPlace(Pageable pageable, String placeId);
}