package school.hei.haapi.repository.dao;

import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Fee;

@Repository
@AllArgsConstructor
public class FeeDao {
  private EntityManager entityManager;

  public void updateFeesStatusWithBatch(List<Fee> feeToUpdate) {
    for (int i = 0; i < feeToUpdate.size(); i++) {
      if (i > 0 && i % feeToUpdate.size() == 0) {
        entityManager.flush();
        entityManager.clear();
      }
      entityManager.persist(feeToUpdate.get(i));
    }
  }
}
