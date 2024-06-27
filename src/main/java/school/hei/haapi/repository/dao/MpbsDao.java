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
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Mpbs.Mpbs;

@Repository
@AllArgsConstructor
public class MpbsDao {
  private EntityManager entityManager;

  public List<Mpbs> findMpbsBetween(Instant begin, Instant end) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery query = builder.createQuery(Mpbs.class);
    Root<Mpbs> root = query.from(Mpbs.class);
    List<Predicate> predicates = new ArrayList<>();

    predicates.add(builder.and(builder.between(root.get("creationDatetime"), begin, end)));

    query.distinct(true).where(predicates.toArray(new Predicate[0]));

    return entityManager.createQuery(query).getResultList();
  }
}
