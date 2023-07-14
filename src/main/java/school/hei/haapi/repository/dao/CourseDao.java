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
import school.hei.haapi.endpoint.rest.model.CourseStatus;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.StudentCourse;
import school.hei.haapi.model.User;

import static school.hei.haapi.endpoint.rest.model.CourseStatus.LINKED;

@Repository
@AllArgsConstructor
public class CourseDao {
  private final EntityManager entityManager;

  public List<Course> findByCriteria(String code, String name, Integer credits,
                                     String teacherFirstName, String teacherLastName,
                                     String creditsOrder, String codeOrder,
                                     Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Course> query = builder.createQuery(Course.class);
    Root<Course> root = query.from(Course.class);
    Join<Course, User> teacher = root.join("mainTeacher");

    List<Predicate> predicates = new ArrayList<>();

    if (code != null) {
      predicates.add(
          builder.or(
              builder.like(builder.lower(root.get("code")), "%" + code + "%"),
              builder.like(root.get("code"), "%" + code + "%")
          )
      );
    }

    if (name != null) {
      predicates.add(builder.or(
          builder.like(builder.lower(root.get("name")), "%" + name + "%"),
          builder.like(root.get("name"), "%" + name + "%")
      ));
    }

    if (teacherFirstName != null) {
      predicates.add(builder.like(builder.lower(teacher.get("firstName")),
          "%" + teacherFirstName.toLowerCase() + "%"));
    }
    if (teacherLastName != null) {
      predicates.add(builder.like(builder.lower(teacher.get("lastName")),
          "%" + teacherLastName.toLowerCase() + "%"));
    }

    Predicate hasCredits = credits != null
        ? builder.or(
        builder.equal(root.get("credits"), credits)
    ) : null;

    if (hasCredits != null) {
      predicates.add(hasCredits);
    }

    query.where(builder.and(predicates.toArray(new Predicate[0])));

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

    return entityManager.createQuery(query)
        .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }

  private Order getOrder(Root<Course> root, CriteriaBuilder builder, String order,
                         String property) {
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

  public List<Course> getByUserIdAndStatus(String userId, CourseStatus status) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Course> query = builder.createQuery(Course.class);
    Root<Course> courseRoot = query.from(Course.class);

    Join<Course, StudentCourse> studentCourse =
        courseRoot.join("studentCourses", JoinType.LEFT);

    Join<StudentCourse, User> user = studentCourse.join("student", JoinType.LEFT);

    Predicate hasUserId =
        builder.equal(user.get("id"), userId);
    if (status == null) {
      status = LINKED;
    }
    Predicate hasStatus = builder.equal(studentCourse.get("status"), status);
    query.where(builder.and(hasUserId, hasStatus))
        .distinct(true);
    return entityManager.createQuery(query).getResultList();
  }

}
