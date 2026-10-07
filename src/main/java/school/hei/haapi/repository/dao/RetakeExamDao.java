package school.hei.haapi.repository.dao;

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
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.RetakeExam;
import school.hei.haapi.model.RetakeExamStatus;

@Repository
@AllArgsConstructor
public class RetakeExamDao {
  private static final String COURSE = "course";

  private final EntityManager entityManager;

  public List<RetakeExam> filterByCriteria(
      String sessionId,
      String studentId,
      String studentRef,
      String courseId,
      String courseCode,
      List<RetakeExamStatus> statuses,
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
      predicates.add(builder.equal(root.get(COURSE).get("id"), courseId));
    }

    if (courseCode != null) {
      predicates.add(
          builder.like(
              builder.lower(root.get(COURSE).get("code")), "%" + courseCode.toLowerCase() + "%"));
    }

    if (statuses != null && !statuses.isEmpty()) {
      predicates.add(root.get("status").in(statuses));
    }

    query.where(predicates.toArray(new Predicate[0]));

    return entityManager
        .createQuery(query)
        .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }

  public List<Course> findDistinctCoursesBySessionId(
      String sessionId, String courseCode, Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Course> query = builder.createQuery(Course.class);
    Root<RetakeExam> root = query.from(RetakeExam.class);
    Join<RetakeExam, Course> courseJoin = root.join(COURSE);

    List<Predicate> predicates = new ArrayList<>();

    if (sessionId != null) {
      predicates.add(builder.equal(root.get("session").get("id"), sessionId));
    }

    if (courseCode != null) {
      predicates.add(
          builder.like(
              builder.lower(courseJoin.get("code")), "%" + courseCode.toLowerCase() + "%"));
    }

    query
        .select(courseJoin)
        .distinct(true)
        .where(predicates.toArray(new Predicate[0]))
        .orderBy(builder.asc(courseJoin.get("code")));

    return entityManager
        .createQuery(query)
        .setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }
}
