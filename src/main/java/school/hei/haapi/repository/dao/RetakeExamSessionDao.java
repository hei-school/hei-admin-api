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
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.model.RetakeExamSession;

@Repository
@AllArgsConstructor
public class RetakeExamSessionDao {
  private final EntityManager entityManager;

  public List<RetakeExamSession> filterByCriteria(
      String title, List<StudentLevel> studentLevels, Pageable pageable, Instant from, Instant to) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<RetakeExamSession> query = builder.createQuery(RetakeExamSession.class);
    Root<RetakeExamSession> root = query.from(RetakeExamSession.class);
    ArrayList<Predicate> predicates = new ArrayList<>();
    if (title != null && !title.isEmpty()) {
      predicates.add(
          builder.like(builder.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
    }

    if (studentLevels != null && !studentLevels.isEmpty()) {
      Join<RetakeExamSession, StudentLevel> join = root.join("studentLevels", JoinType.INNER);
      predicates.add(join.in(studentLevels));
    }

    addRetakeExamDateRangePredicates(from, to, predicates, builder, root);
    query
        .distinct(true)
        .where(predicates.toArray(new Predicate[0]))
        .orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder));
    return entityManager
        .createQuery(query)
        .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }

  private static void addRetakeExamDateRangePredicates(
      Instant from,
      Instant to,
      ArrayList<Predicate> predicates,
      CriteriaBuilder builder,
      Root<RetakeExamSession> root) {
    if (from != null) {
      predicates.add(builder.greaterThanOrEqualTo(root.get("dateFrom"), from));
    }
    if (to != null) {
      predicates.add(builder.lessThanOrEqualTo(root.get("dateTo"), to));
    }
  }
}
