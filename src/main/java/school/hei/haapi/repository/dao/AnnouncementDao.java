package school.hei.haapi.repository.dao;

import static jakarta.persistence.criteria.JoinType.LEFT;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import school.hei.haapi.endpoint.rest.model.Scope;
import school.hei.haapi.model.Announcement;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.User;

@Repository
@AllArgsConstructor
public class AnnouncementDao {
  private final EntityManager entityManager;

  public List<Announcement> findByCriteria(
      Instant from,
      Instant to,
      String authorRef,
      List<Scope> scopes,
      String groupRef,
      Pageable pageable) {

    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Announcement> query = builder.createQuery(Announcement.class);
    Root<Announcement> root = query.from(Announcement.class);
    Join<Announcement, User> joinUser = root.join("author");
    Join<Announcement, Group> joinGroup = root.join("groups", LEFT);

    List<Predicate> predicates = new ArrayList<>();

    if (from != null) {
      predicates.add(builder.greaterThanOrEqualTo(root.get("creationDatetime"), from));
    }

    if (to != null) {
      predicates.add(builder.lessThanOrEqualTo(root.get("creationDatetime"), to));
    }

    if (authorRef != null) {
      predicates.add(
          builder.or(
              builder.like(builder.lower(joinUser.get("ref")), "%" + authorRef + "%"),
              builder.like(joinUser.get("ref"), "%" + authorRef + "%")));
    }

    if (scopes != null) {
      predicates.add(root.get("scope").in(scopes));
    }

    if (groupRef != null) {
      predicates.add(
          builder.or(
              builder.like(builder.lower(joinGroup.get("ref")), "%" + groupRef + "%"),
              builder.like(joinGroup.get("ref"), "%" + groupRef + "%")));
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
