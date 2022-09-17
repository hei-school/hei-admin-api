package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Event;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event,String> {

    List<Event> findByResponsibleId(String id_responsible,Pageable  pageable);


    List<Event> findByNameContainingIgnoreCase(Pageable pageable, String name);
}
