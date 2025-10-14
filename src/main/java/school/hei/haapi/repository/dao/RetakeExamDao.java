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
import org.springframework.stereotype.Component;
import school.hei.haapi.model.RetakeExam;

@Component
@AllArgsConstructor
public class RetakeExamDao {
  private final EntityManager entityManager;

  public List<RetakeExam> filterByCriteria(
      String sessionId,
      String studentId,
      String studentRef,
      String courseId,
      String courseCode,
      Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<RetakeExam> query = builder.createQuery(RetakeExam.class);
    Root<RetakeExam> root = query.from(RetakeExam.class);

    List<Predicate> predicates = new ArrayList<>();

    if (studentId != null) {
      predicates.add(builder.equal(root.get("student").get("id"), studentId));
    }

    if (studentRef != null) {
      predicates.add(
          builder.like(
              builder.lower(root.get("student").get("ref")), "%" + studentRef.toLowerCase() + "%"));
    }

    if (sessionId != null) {
      predicates.add(builder.equal(root.get("session").get("id"), sessionId));
    }

    if (courseId != null) {
      predicates.add(builder.equal(root.get("course").get("id"), courseId));
    }

    if (courseCode != null) {
      predicates.add(
          builder.like(
              builder.lower(root.get("course").get("code")), "%" + courseCode.toLowerCase() + "%"));
    }

    query.where(predicates.toArray(new Predicate[0]));

    return entityManager
        .createQuery(query)
        .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }
}
