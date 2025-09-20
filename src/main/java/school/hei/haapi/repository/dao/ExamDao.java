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
import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Group;

@Repository
@AllArgsConstructor
public class ExamDao {
  private final EntityManager entityManager;

  public List<Exam> findByCriteria(
      Pageable pageable,
      String title,
      String courseCode,
      String teacherId,
      String groupRef,
      Instant from,
      Instant to) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Exam> query = builder.createQuery(Exam.class);
    Root<Exam> root = query.from(Exam.class);
    ArrayList<Predicate> predicates = new ArrayList<>();
    if (title != null && !title.isEmpty()) {
      predicates.add(
          builder.like(builder.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
    }

    Join<Exam, CourseAssignment> courseAssignmentJoin =
        root.join("courseAssignment", JoinType.LEFT);
    predicates.add(builder.equal(courseAssignmentJoin.get("isDeleted"), false));
    if (courseCode != null && !courseCode.isEmpty()) {
      Join<CourseAssignment, Course> courseJoin =
          courseAssignmentJoin.join("course", JoinType.LEFT);
      predicates.add(
          builder.like(
              builder.lower(courseJoin.get("code")), "%" + courseCode.toLowerCase() + "%"));
    }

    if (teacherId != null && !teacherId.isEmpty()) {
      predicates.add(builder.equal(courseAssignmentJoin.get("mainTeacher").get("id"), teacherId));
    }

    if (groupRef != null && !groupRef.isEmpty()) {
      Join<CourseAssignment, Group> groupJoin = courseAssignmentJoin.join("groups", JoinType.LEFT);
      predicates.add(
          builder.like(builder.lower(groupJoin.get("ref")), "%" + groupRef.toLowerCase() + "%"));
    }
    addExaminationDateRangePredicates(from, to, predicates, builder, root);
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

  private static void addExaminationDateRangePredicates(
      Instant from,
      Instant to,
      ArrayList<Predicate> predicates,
      CriteriaBuilder builder,
      Root<Exam> root) {
    if (from != null && to != null) {
      predicates.add(builder.between(root.get("examinationDate"), from, to));
    } else if (from == null && to != null) {
      predicates.add(builder.between(root.get("examinationDate"), Instant.now(), to));
    } else if (to == null && from != null) {
      predicates.add(builder.between(root.get("examinationDate"), from, Instant.now()));
    }
  }
}
