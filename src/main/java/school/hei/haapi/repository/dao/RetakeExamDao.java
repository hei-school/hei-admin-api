package school.hei.haapi.repository.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
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

    if (studentId != null) {
      query.where(builder.equal(root.get("student").get("id"), studentId));
    }

    if (studentRef != null) {
      query.where(builder.equal(root.get("student").get("ref"), studentRef));
    }

    if (sessionId != null) {
      query.where(builder.equal(root.get("session").get("id"), sessionId));
    }

    if (courseId != null) {
      query.where(builder.equal(root.get("course").get("id"), courseId));
    }
    if (courseCode != null) {
      query.where(builder.equal(root.get("course").get("code"), courseCode));
    }

    return entityManager
        .createQuery(query)
        .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }
}
