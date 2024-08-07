package school.hei.haapi.repository.dao;

import static jakarta.persistence.criteria.JoinType.LEFT;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.Promotion;

@Repository
@AllArgsConstructor
public class PromotionDao {
  private final EntityManager entityManager;

  public List<Promotion> findByCriteria(
      String name, String ref, String groupRef, Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Promotion> query = builder.createQuery(Promotion.class);
    Root<Promotion> root = query.from(Promotion.class);
    Join<Promotion, Group> join = root.join("groups", LEFT);

    List<Predicate> predicates = new ArrayList<>();

    if (name != null) {
      predicates.add(
          builder.or(
              builder.like(builder.lower(root.get("name")), "%" + name + "%"),
              builder.like(root.get("name"), "%" + name + "%")));
    }

    if (ref != null) {
      predicates.add(
          builder.or(
              builder.like(builder.lower(root.get("ref")), "%" + ref + "%"),
              builder.like(root.get("ref"), "%" + ref + "%")));
    }

    if (groupRef != null) {
      predicates.add(
          builder.or(
              builder.like(builder.lower(join.get("ref")), "%" + groupRef + "%"),
              builder.like(join.get("ref"), "%" + groupRef + "%")));
    }

    if (!predicates.isEmpty()) {
      query.where(predicates.toArray(new Predicate[0])).distinct(true);
    }

    query.orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder));

    return entityManager
        .createQuery(query)
        .setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }
}
