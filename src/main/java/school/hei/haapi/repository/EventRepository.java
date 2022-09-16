package school.hei.haapi.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Event;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event,String> {
    List<Event> findByNameContainingIgnoreCase(String name, Pageable pageable);

    List<Event> findAllBySupervisor_Id(String supervisorId, Pageable pageable);
}
