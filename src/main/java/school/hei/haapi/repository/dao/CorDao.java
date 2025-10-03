package school.hei.haapi.repository.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Cor;
import school.hei.haapi.model.CorLastComment;
import school.hei.haapi.model.CorStatus;
import school.hei.haapi.model.exception.NotImplementedException;

@Repository
@AllArgsConstructor
public class CorDao {
  private final EntityManager entityManager;

  public List<Cor> findByCriteria(
      Instant from,
      Instant to,
      String studentRef,
      String groupRef,
      List<CorStatus> statuses,
      Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Cor> query = builder.createQuery(Cor.class);
    Root<Cor> root = query.from(Cor.class);

    List<Predicate> predicates = new ArrayList<>();

    if (from != null) {
      predicates.add(builder.greaterThanOrEqualTo(root.get("creationDatetime"), from));
    }

    if (to != null) {
      predicates.add(builder.lessThanOrEqualTo(root.get("creationDatetime"), to));
    }

    if (studentRef != null && !studentRef.isEmpty()) {
      predicates.add(
          builder.like(
              builder.lower(root.get("student").get("ref")), "%" + studentRef.toLowerCase() + "%"));
    }

    if (groupRef != null && !groupRef.isEmpty()) {
      throw new NotImplementedException("Filter by group ref not implemented");
    }

    if (statuses != null && !statuses.isEmpty()) {
      if (statuses.size() > 1) {
        throw new NotImplementedException("Filter by statuses not implemented");
      }
      Join<Cor, CorLastComment> lastCommentJoin = root.join("lastComment", JoinType.LEFT);

      Predicate matchStatus = builder.equal(lastCommentJoin.get("status"), statuses.getFirst());
      Predicate inProgressWithNull =
          builder.and(
              builder.equal(
                  builder.literal(CorStatus.IN_PROGRESS).as(CorStatus.class), statuses.getFirst()),
              builder.isNull(lastCommentJoin.get("status")));

      predicates.add(builder.or(matchStatus, inProgressWithNull));
    }

    query.where(predicates.toArray(new Predicate[0]));

    if (pageable.isUnpaged()) {
      return entityManager.createQuery(query).getResultList();
    }

    return entityManager
        .createQuery(query)
        .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }
}
