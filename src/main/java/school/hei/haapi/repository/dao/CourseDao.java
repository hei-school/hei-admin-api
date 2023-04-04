package school.hei.haapi.repository.dao;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.User;

import static school.hei.haapi.service.utils.DataFormatterUtils.handleNull;

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

    Predicate hasCode =
        builder.like(builder.lower(root.get("code")), "%" + handleNull(code) + "%");

    Predicate hasName =
        builder.like(builder.lower(root.get("name")), "%" + handleNull(name) + "%");


    Predicate hasCredits = credits != null ?
        builder.or(
            builder.equal(root.get("credits"), credits)
        ) : null;


    Predicate hasTeacherFirstName = builder.like(builder.lower(teacher.get("firstName")),
        "%" +
            handleNull(teacherFirstName) + "%");


    Predicate hasTeacherLastName = builder.like(builder.lower(teacher.get("lastName")),
        "%" + handleNull(teacherLastName) + "%");


    List<Predicate> predicates = new ArrayList<>();

    if (hasCode != null) {
      predicates.add(hasCode);
    }

    if (hasName != null) {
      predicates.add(hasName);
    }

    if (hasTeacherFirstName != null) {
      predicates.add(hasTeacherFirstName);
    }

    if (hasTeacherLastName != null) {
      predicates.add(hasTeacherLastName);
    }

    /* To prevent in case credits is null
      and will not affect the other filters
    */
    if (hasCredits != null) {
      predicates.add(hasCredits);
    }

    query.where(builder.and(predicates.toArray(new Predicate[0])));

    Order creditsSortOrder = (creditsOrder != null && !creditsOrder.isEmpty()) ?
        (creditsOrder.equalsIgnoreCase("ASC") ?
            builder.asc(root.get("credits")) :
            (creditsOrder.equalsIgnoreCase("DESC") ?
                builder.desc(root.get("credits")) : null))
        : null;

    Order codeSortOrder = (codeOrder != null && !codeOrder.isEmpty()) ?
        codeOrder.equalsIgnoreCase("ASC") ? builder.asc(root.get("code")) :
            codeOrder.equalsIgnoreCase("DESC") ? builder.desc(root.get("code")) : null
        : null;


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

}
