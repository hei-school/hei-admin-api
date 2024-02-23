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
import school.hei.haapi.model.FeeTemplate;

@Repository
@AllArgsConstructor
public class FeeTemplateDao {
  private final EntityManager entityManager;

  public List<FeeTemplate> findByCriteria(
      String name, Integer amount, Integer numberOfPayments, Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<FeeTemplate> query = builder.createQuery(FeeTemplate.class);
    Root<FeeTemplate> root = query.from(FeeTemplate.class);

    List<Predicate> predicates = new ArrayList<>();

    if (name != null) {
      predicates.add(
          builder.or(
              builder.like(builder.lower(root.get("name")), "%" + name + "%"),
              builder.like(root.get("name"), "%" + name + "%")));
    }

    if (amount != null) {
      predicates.add(builder.or(builder.equal(root.get("amount"), amount)));
    }

    if (numberOfPayments != null) {
      predicates.add(builder.or(builder.equal(root.get("numberOfPayments"), numberOfPayments)));
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
