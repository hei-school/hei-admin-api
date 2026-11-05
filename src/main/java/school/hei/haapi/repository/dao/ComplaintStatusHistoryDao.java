package school.hei.haapi.repository.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.ComplaintStatusHistory;

@Repository
@AllArgsConstructor
public class ComplaintStatusHistoryDao {
  private static final String DATETIME_FIELD = "datetime";
  private final EntityManager entityManager;

  public List<ComplaintStatusHistory> findByCriteria(
      String complaintId, Instant from, Instant to, Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<ComplaintStatusHistory> query = builder.createQuery(ComplaintStatusHistory.class);
    Root<ComplaintStatusHistory> root = query.from(ComplaintStatusHistory.class);

    List<Predicate> predicates = new ArrayList<>();
    predicates.add(builder.equal(root.get("complaint").get("id"), complaintId));

    if (from != null) {
      predicates.add(builder.greaterThanOrEqualTo(root.get(DATETIME_FIELD), from));
    }

    if (to != null) {
      predicates.add(builder.lessThanOrEqualTo(root.get(DATETIME_FIELD), to));
    }

    query.where(predicates.toArray(new Predicate[0]));
    query.orderBy(builder.desc(root.get(DATETIME_FIELD)));

    if (pageable.isUnpaged()) {
      return entityManager.createQuery(query).getResultList();
    }

    return entityManager
        .createQuery(query)
        .setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }
}
