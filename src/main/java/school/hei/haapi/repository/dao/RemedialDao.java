package school.hei.haapi.repository.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.*;

@Component
@AllArgsConstructor
public class RemedialDao {
  private final EntityManager entityManager;

  public List<Remedial> findByCriteria(
      Pageable pageable,
      String teacherId,
      String title,
      String courseCode,
      String groupRef,
      Instant remedialDateStart,
      Instant remedialDateEnd) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Remedial> query = builder.createQuery(Remedial.class);
    Root<Remedial> root = query.from(Remedial.class);
    ArrayList<Predicate> predicates = new ArrayList<>();

    if (title != null && !title.isEmpty()) {
      predicates.add(
          builder.like(builder.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
    }
    Join<Exam, CourseAssignment> courseAssignmentJoin =
        root.join("courseAssignment", JoinType.LEFT);
    predicates.add(builder.equal(courseAssignmentJoin.get("isDeleted"), false));

    if (teacherId != null && !teacherId.isEmpty()) {
      predicates.add(builder.equal(courseAssignmentJoin.get("mainTeacher").get("id"), teacherId));
    }

    if (courseCode != null && !courseCode.isEmpty()) {
      Join<CourseAssignment, Course> courseJoin =
          courseAssignmentJoin.join("course", JoinType.LEFT);
      predicates.add(
          builder.like(
              builder.lower(courseJoin.get("code")), "%" + courseCode.toLowerCase() + "%"));
    }
    if (groupRef != null && !groupRef.isEmpty()) {
      Join<CourseAssignment, Group> groupJoin = courseAssignmentJoin.join("groups", JoinType.LEFT);
      predicates.add(
          builder.like(builder.lower(groupJoin.get("ref")), "%" + groupRef.toLowerCase() + "%"));
    }
    addRemedialDateRangePredicates(remedialDateStart, remedialDateEnd, predicates, builder, root);
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

  private static void addRemedialDateRangePredicates(
      Instant remedialDateStart,
      Instant remedialDateEnd,
      ArrayList<Predicate> predicates,
      CriteriaBuilder builder,
      Root<Remedial> root) {
    if (remedialDateStart != null && remedialDateEnd != null) {
      predicates.add(builder.between(root.get("remedialDate"), remedialDateStart, remedialDateEnd));
    } else if (remedialDateStart == null && remedialDateEnd != null) {
      predicates.add(builder.lessThanOrEqualTo(root.get("remedialDate"), remedialDateEnd));
    } else if (remedialDateStart != null) {
      predicates.add(builder.greaterThanOrEqualTo(root.get("remedialDate"), remedialDateStart));
    }
  }
}
