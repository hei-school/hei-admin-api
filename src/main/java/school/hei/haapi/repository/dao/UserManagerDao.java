package school.hei.haapi.repository.dao;

import static jakarta.persistence.criteria.JoinType.LEFT;
import static school.hei.haapi.endpoint.rest.model.WorkStudyStatus.HAVE_BEEN_WORKING;
import static school.hei.haapi.endpoint.rest.model.WorkStudyStatus.WILL_BE_WORKING;
import static school.hei.haapi.endpoint.rest.model.WorkStudyStatus.WORKING;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import school.hei.haapi.endpoint.rest.model.WorkStudyStatus;
import school.hei.haapi.model.AwardedCourse;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.User;
import school.hei.haapi.model.WorkDocument;

@Repository
@AllArgsConstructor
public class UserManagerDao {
  private EntityManager entityManager;

  public List<User> findByCriteria(
      User.Role role,
      String ref,
      String firstName,
      String lastName,
      Pageable pageable,
      User.Status status,
      User.Sex sex,
      WorkStudyStatus workStatus,
      Instant commitmentBeginDate,
      String courseId,
      Instant commitmentComparison) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<User> query = builder.createQuery(User.class);
    Root<User> root = query.from(User.class);
    Join<User, WorkDocument> workDocumentJoin = root.join("workDocuments", LEFT);
    Join<User, AwardedCourse> awardedCourseJoin = root.join("awardedCourses", LEFT);
    Join<AwardedCourse, Course> courseJoin = awardedCourseJoin.join("course", LEFT);
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

    if (courseId != null && !courseId.isEmpty() && !courseId.isBlank()) {
      Expression<String> courseIdExpression = courseJoin.get("id");
      predicate = builder.and(predicate, builder.equal(courseIdExpression, courseId));
    }

    if (commitmentBeginDate != null) {
      Expression<Instant> commitmentBeginExpression = workDocumentJoin.get("commitmentBegin");
      predicate =
          builder.and(
              predicate,
              builder.greaterThanOrEqualTo(commitmentBeginExpression, commitmentBeginDate));
    }

    if (firstName != null && !firstName.isEmpty()) {
      predicate = builder.and(predicate, hasUserFirstName);
    }

    if (status != null) {
      predicate = builder.and(predicate, builder.equal(root.get("status"), status));
    }

    if (sex != null) {
      predicate = builder.and(predicate, builder.equal(root.get("sex"), sex));
    }

    if (WORKING.equals(workStatus)) {
      predicate =
          builder.and(
              predicate,
              builder.lessThanOrEqualTo(
                  workDocumentJoin.get("commitmentBegin"), commitmentComparison));
    }
    if (HAVE_BEEN_WORKING.equals(workStatus)) {
      predicate =
          builder.and(
              predicate,
              builder.lessThanOrEqualTo(
                  workDocumentJoin.get("commitmentEnd"), commitmentComparison));
    }
    if (WILL_BE_WORKING.equals(workStatus)) {
      predicate =
          builder.and(
              predicate,
              builder.greaterThanOrEqualTo(
                  workDocumentJoin.get("commitmentBegin"), commitmentComparison));
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
    Join<User, AwardedCourse> awardedCourse = root.join("awardedCourses");
    Join<AwardedCourse, Course> course = awardedCourse.join("course");

    Predicate hasCourseId = builder.equal(course.get("id"), courseId);
    Predicate hasUserRole = builder.equal(root.get("role"), role);
    query
        .distinct(true)
        .where(builder.and(hasUserRole, hasCourseId))
        .orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder));

    return entityManager
        .createQuery(query)
        .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }
}
