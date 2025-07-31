package school.hei.haapi.repository.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.User;

@Repository
@AllArgsConstructor
public class CourseAssignmentDao {
  private final EntityManager entityManager;

  public List<CourseAssignment> findByCriteria(
      String teacherId, String courseId, StudentLevel studentLevel, Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<CourseAssignment> query = builder.createQuery(CourseAssignment.class);
    Root<CourseAssignment> root = query.from(CourseAssignment.class);
    Join<CourseAssignment, User> teacher = root.join("mainTeacher", JoinType.INNER);
    Join<CourseAssignment, Course> courses = root.join("course", JoinType.INNER);

    List<Predicate> predicates = new ArrayList<>();

    if (teacherId != null) {
      predicates.add(
          builder.or(
              builder.like(builder.lower(teacher.get("id")), "%" + teacherId + "%"),
              builder.like(teacher.get("id"), "%" + teacherId + "%")));
    }

    if (courseId != null) {
      predicates.add(
          builder.or(
              builder.like(builder.lower(courses.get("id")), "%" + courseId + "%"),
              builder.like(courses.get("id"), "%" + courseId + "%")));
    }

    if (studentLevel != null) {
      predicates.add(builder.equal(courses.get("studentLevel"), studentLevel));
    }

    predicates.add(builder.equal(root.get("isDeleted"), false));
    query.where(builder.and(predicates.toArray(new Predicate[0]))).distinct(true);

    if (pageable.isUnpaged()) {
      return entityManager.createQuery(query).getResultList();
    }

    return entityManager
        .createQuery(query)
        .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }
}
