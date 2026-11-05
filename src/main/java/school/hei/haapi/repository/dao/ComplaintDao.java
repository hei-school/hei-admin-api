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
import school.hei.haapi.model.Complaint;
import school.hei.haapi.model.ComplaintStatus;
import school.hei.haapi.model.ComplaintType;

@Repository
@AllArgsConstructor
public class ComplaintDao {
  private static final String CREATED_AT_FIELD = "createdAt";
  private final EntityManager entityManager;

  public List<Complaint> findByCriteria(
      List<ComplaintType> types,
      List<ComplaintStatus> statuses,
      String managerId,
      String assigneeId,
      Instant from,
      Instant to,
      Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Complaint> query = builder.createQuery(Complaint.class);
    Root<Complaint> root = query.from(Complaint.class);

    List<Predicate> predicates = new ArrayList<>();

    if (types != null && !types.isEmpty()) {
      predicates.add(root.get("type").in(types));
    }

    if (statuses != null && !statuses.isEmpty()) {
      predicates.add(root.get("status").in(statuses));
    }

    if (from != null) {
      predicates.add(builder.greaterThanOrEqualTo(root.get(CREATED_AT_FIELD), from));
    }

    if (to != null) {
      predicates.add(builder.lessThanOrEqualTo(root.get(CREATED_AT_FIELD), to));
    }

    if (managerId != null && !managerId.isEmpty()) {
      var managerJoin = root.join("assignees");
      predicates.add(builder.equal(managerJoin.get("user").get("id"), managerId));
      predicates.add(builder.isNull(managerJoin.get("assignedBy")));
    }

    if (assigneeId != null && !assigneeId.isEmpty()) {
      var assigneeJoin = root.join("assignees");
      predicates.add(builder.equal(assigneeJoin.get("user").get("id"), assigneeId));
    }

    query.distinct(true).where(predicates.toArray(new Predicate[0]));
    query.orderBy(builder.desc(root.get(CREATED_AT_FIELD)));

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
