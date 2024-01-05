package school.hei.haapi.repository.dao;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.FeeType;

@Repository
@AllArgsConstructor
public class FeeTypeDao {
  private final EntityManager entityManager;

  public List<FeeType> findByCriteria(
      String name, Integer totalAmount, Integer numberOfMonths, Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<FeeType> query = builder.createQuery(FeeType.class);
    Root<FeeType> root = query.from(FeeType.class);

    List<Predicate> predicates = new ArrayList<>();

    if (name != null) {
      predicates.add(
          builder.or(
              builder.like(builder.lower(root.get("name")), "%" + name + "%"),
              builder.like(root.get("name"), "%" + name + "%")));
    }

    if (totalAmount != null) {
      predicates.add(builder.or(builder.equal(root.get("totalAmount"), totalAmount)));
    }

    if (numberOfMonths != null) {
      predicates.add(builder.or(builder.equal(root.get("numberOfMonths"), numberOfMonths)));
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
