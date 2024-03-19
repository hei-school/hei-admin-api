package school.hei.haapi.repository.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import school.hei.haapi.endpoint.rest.model.EventType;
import school.hei.haapi.model.Event;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Repository
@AllArgsConstructor
public class EventDao {
  private final EntityManager entityManager;

  public List<Event> findByCriteria(
          Instant from, Instant to, EventType eventType, Pageable pageable) {

    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Event> query = builder.createQuery(Event.class);
    Root<Event> root = query.from(Event.class);

    List<Predicate> predicates = new ArrayList<>();

    if (from != null) {
      predicates.add(builder.greaterThanOrEqualTo(root.get("begin"), from));
    }

    if (to != null) {
      predicates.add(builder.lessThanOrEqualTo(root.get("begin"), to));
    }

    if(eventType != null){
      predicates.add(builder.equal(root.get("type"), eventType));
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
