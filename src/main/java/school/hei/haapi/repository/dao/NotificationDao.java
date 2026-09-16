package school.hei.haapi.repository.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Notification;
import school.hei.haapi.model.NotificationResolutionStatus;

@Repository
@AllArgsConstructor
public class NotificationDao {
  private final EntityManager entityManager;

  public List<Notification> filterByCriteria(
      String recipientId,
      Boolean read,
      NotificationResolutionStatus resolutionStatus,
      Pageable pageable) {
    var builder = entityManager.getCriteriaBuilder();
    var query = builder.createQuery(Notification.class);
    var root = query.from(Notification.class);

    List<Predicate> predicates = new ArrayList<>();
    predicates.add(builder.equal(root.get("recipient").get("id"), recipientId));
    if (read != null) {
      predicates.add(builder.equal(root.get("read"), read));
    }
    if (resolutionStatus != null) {
      predicates.add(builder.equal(root.get("resolutionStatus"), resolutionStatus));
    }

    query.where(predicates.toArray(new Predicate[0]));
    query.orderBy(builder.desc(root.get("creationDatetime")));

    return entityManager
        .createQuery(query)
        .setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }
}
