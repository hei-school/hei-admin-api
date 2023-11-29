package school.hei.haapi.repository.dao;

import java.util.Arrays;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import school.hei.haapi.endpoint.rest.model.EnableStatus;
import school.hei.haapi.endpoint.rest.model.Sex;
import school.hei.haapi.model.AwardedCourse;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;

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
      EnableStatus status,
      Sex sex) {
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

    if (status != null) {
      predicate = builder.and(predicate, builder.equal(root.get("status"), toDomainStatus(status)));
    }

    if (sex != null) {
      predicate = builder.and(predicate, builder.equal(root.get("sex"), toDomainSex(sex)));
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

  private User.Sex toDomainSex(Sex sex) {
    List<User.Sex> domainSex = Arrays.stream(User.Sex.values()).toList();
    return switch (sex) {
      case F -> User.Sex.F;
      case M -> User.Sex.M;
      default -> throw new BadRequestException("Sex must be type of: " + domainSex.toString());
    };
  }

  private User.Status toDomainStatus(EnableStatus status) {
    List<User.Status> domainStatus = Arrays.stream(User.Status.values()).toList();
    return switch (status) {
      case ENABLED -> User.Status.ENABLED;
      case DISABLED -> User.Status.DISABLED;
      case SUSPENDED -> User.Status.SUSPENDED;
      default -> throw new BadRequestException("Status must be type of: " + domainStatus.toString());
    };
  }
}