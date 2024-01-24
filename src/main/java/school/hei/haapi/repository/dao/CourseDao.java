package school.hei.haapi.repository.dao;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.AwardedCourse;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.User;

@Repository
@AllArgsConstructor
public class CourseDao {
  private final EntityManager entityManager;

  // todo: to review

  public List<Course> findByCriteria(
      String code,
      String name,
      Integer credits,
      String teacherFirstName,
      String teacherLastName,
      String creditsOrder,
      String codeOrder,
      Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Course> query = builder.createQuery(Course.class);
    Root<Course> root = query.from(Course.class);
    Join<Course, AwardedCourse> awardedCourses = root.join("awardedCourses", JoinType.LEFT);
    Join<AwardedCourse, User> teacher = awardedCourses.join("mainTeacher", JoinType.LEFT);

    List<Predicate> predicates = new ArrayList<>();

    if (code != null) {
      predicates.add(
          builder.or(
              builder.like(builder.lower(root.get("code")), "%" + code + "%"),
              builder.like(root.get("code"), "%" + code + "%")));
    }

    if (name != null) {
      predicates.add(
          builder.or(
              builder.like(builder.lower(root.get("name")), "%" + name.toLowerCase() + "%"),
              builder.like(root.get("name"), "%" + name + "%")));
    }

    if (teacherLastName != null && !teacherLastName.isBlank()) {
      predicates.add(
          builder.like(
              builder.lower(teacher.get("lastName")), "%" + teacherLastName.toLowerCase() + "%"));
    }

    if (teacherFirstName != null && !teacherFirstName.isBlank()) {
      predicates.add(
          builder.like(
              builder.lower(teacher.get("firstName")), "%" + teacherFirstName.toLowerCase() + "%"));
    }

    predicates.add(builder.isFalse(root.get("isDeleted")));

    Predicate hasCredits =
        credits != null ? builder.or(builder.equal(root.get("credits"), credits)) : null;

    if (hasCredits != null) {
      predicates.add(hasCredits);
    }

    query.where(builder.and(predicates.toArray(new Predicate[0]))).distinct(true);

    Order creditsSortOrder = getOrder(root, builder, creditsOrder, "credits");
    Order codeSortOrder = getOrder(root, builder, codeOrder, "code");

    List<Order> orders = new ArrayList<>();
    if (creditsSortOrder != null) {
      orders.add(creditsSortOrder);
    }
    if (codeSortOrder != null) {
      orders.add(codeSortOrder);
    }
    if (orders.isEmpty()) {
      query.orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder));
    } else {
      query.orderBy(orders);
    }

    return entityManager
        .createQuery(query)
        .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }

  private Order getOrder(
      Root<Course> root, CriteriaBuilder builder, String order, String property) {
    if (order == null || order.isEmpty()) {
      return null;
    }
    if (order.equalsIgnoreCase("ASC")) {
      return builder.asc(root.get(property));
    } else if (order.equalsIgnoreCase("DESC")) {
      return builder.desc(root.get(property));
    } else {
      return null;
    }
  }
}
