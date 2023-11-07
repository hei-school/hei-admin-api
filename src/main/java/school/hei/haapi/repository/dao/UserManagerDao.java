package school.hei.haapi.repository.dao;

import static school.hei.haapi.endpoint.rest.model.CourseStatus.LINKED;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.StudentCourse;
import school.hei.haapi.model.User;

@Repository
@AllArgsConstructor
public class UserManagerDao {
  private EntityManager entityManager;

  public List<User> findByCriteria(
      User.Role role, String ref, String firstName, String lastName, Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<User> query = builder.createQuery(User.class);
    Root<User> root = query.from(User.class);
    Predicate predicate = builder.conjunction();

    Predicate hasUserRef =
        builder.or(
            builder.like(builder.lower(root.get("ref")), "%" + ref + "%"),
            builder.like(root.get("ref"), "%" + ref + "%"));

    Predicate hasUserFirstName =
        builder.or(
            builder.like(builder.lower(root.get("firstName")), "%" + firstName + "%"),
            builder.like(root.get("firstName"), "%" + firstName + "%"));

    Predicate hasUserLastName =
        builder.or(
            builder.like(builder.lower(root.get("lastName")), "%" + lastName + "%"),
            builder.like(root.get("lastName"), "%" + lastName + "%"));

    Predicate hasUserRole = builder.equal(root.get("role"), role);

    if (firstName != null && !firstName.isEmpty()) {
      predicate = builder.and(predicate, hasUserFirstName);
    }

    predicate = builder.and(predicate, hasUserRole, hasUserRef, hasUserLastName);

    query.where(predicate).orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder));

    return entityManager
        .createQuery(query)
        .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }

  public List<User> findByLinkedCourse(User.Role role, String courseId, Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<User> query = builder.createQuery(User.class);
    Root<User> root = query.from(User.class);
    Join<User, StudentCourse> studentCourse = root.join("studentCourses");
    Join<StudentCourse, Course> course = studentCourse.join("courseId");

    Predicate hasCourseId = builder.equal(course.get("id"), courseId);
    Predicate hasLinkedStatus = builder.equal(studentCourse.get("status"), LINKED);
    Predicate hasUserRole = builder.equal(root.get("role"), role);
    query
        .distinct(true)
        .where(builder.and(hasUserRole, hasCourseId, hasLinkedStatus))
        .orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder));

    return entityManager
        .createQuery(query)
        .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }
}
