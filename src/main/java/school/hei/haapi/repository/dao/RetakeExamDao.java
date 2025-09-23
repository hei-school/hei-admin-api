package school.hei.haapi.repository.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.RetakeExam;

@Component
@AllArgsConstructor
public class RetakeExamDao {
  private final EntityManager entityManager;

  public List<RetakeExam> filterByCriteria(String sessionId, String studentId, Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<RetakeExam> query = builder.createQuery(RetakeExam.class);
    Root<RetakeExam> root = query.from(RetakeExam.class);

    if (studentId != null) {
      query.where(builder.equal(root.get("student").get("id"), studentId));
    }

    if (sessionId != null) {
      query.where(builder.equal(root.get("session").get("id"), sessionId));
    }
    return entityManager
        .createQuery(query)
        .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }
}
