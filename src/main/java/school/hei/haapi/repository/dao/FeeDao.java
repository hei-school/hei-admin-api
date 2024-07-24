package school.hei.haapi.repository.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import school.hei.haapi.endpoint.rest.model.FeeStatusEnum;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.User;

@Repository
@AllArgsConstructor
public class FeeDao {

  private final EntityManager entityManager;

  public List<Fee> getByCriteria(
      FeeStatusEnum status, Boolean isMpbs, String studentRef, Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Fee> query = builder.createQuery(Fee.class);
    Root<Fee> root = query.from(Fee.class);

    List<Predicate> predicates = new ArrayList<>();

    if (status != null) {
      predicates.add(builder.equal(root.get("status"), status));
    }

    if (studentRef != null) {
      Join<Fee, User> join = root.join("student");
      predicates.add(builder.like(join.get("ref"), "%" + studentRef + "%"));
    }

    if (isMpbs != null) {
      predicates.add(
          isMpbs ? builder.isNotNull(root.get("mpbs")) : builder.isNull(root.get("mpbs")));
    }

    query
        .orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder))
        .where(predicates.toArray(new Predicate[0]));

    return entityManager
        .createQuery(query)
        .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }
}
