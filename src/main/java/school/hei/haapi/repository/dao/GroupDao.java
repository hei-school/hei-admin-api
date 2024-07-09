package school.hei.haapi.repository.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Group;

@Repository
@AllArgsConstructor
public class GroupDao {
  private final EntityManager entityManager;

  public List<Group> findByCriteria(String ref, Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Group> query = builder.createQuery(Group.class);
    Root<Group> root = query.from(Group.class);

    List<Predicate> predicates = new ArrayList<>();

    if (ref != null) {
      predicates.add(
          builder.or(
              builder.like(builder.lower(root.get("ref")), "%" + ref + "%"),
              builder.like(root.get("ref"), "%" + ref + "%")));
    }

    query
        .orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder))
        .where(predicates.toArray(new Predicate[0]));
    ;

    return entityManager
        .createQuery(query)
        .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }
}
