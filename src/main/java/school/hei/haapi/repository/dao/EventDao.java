package school.hei.haapi.repository.dao;

import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Event;

@Repository
@AllArgsConstructor
public class EventDao {
  private final EntityManager entityManager;

  public List<Event> findByCriteria(String plannerName) {
    return null;
  }
}
