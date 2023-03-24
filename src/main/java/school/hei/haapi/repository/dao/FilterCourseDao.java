package school.hei.haapi.repository.dao;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.User;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.util.List;

@Repository
@AllArgsConstructor
public class FilterCourseDao {
  private EntityManager entityManager;

  public List<Course> findByCriteria(String code, String name, Integer credits, String teacherFirstName, String teacherLastName,
                                     Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Course> query = builder.createQuery(Course.class);
    Root<Course> root = query.from(Course.class);
      Join<Course, User> teacherJoin = root.join("mainTeacher");

    Predicate hasCourseCode =
        builder.or(
            builder.like(builder.lower(root.get("code")), "%" + code + "%"),
            builder.like(root.get("code"), "%" + code + "%")
        );

    Predicate hasCourseName =
        builder.or(
            builder.like(builder.lower(root.get("name")), "%" + name + "%"),
            builder.like(root.get("name"), "%" + name + "%")
        );

    Predicate hasCourseCredits =
      builder.or(
              builder.equal(root.get("credits"), credits),
              builder.greaterThanOrEqualTo(root.get("credits"), 0)
      );

      Predicate hasCourseTeacherFirstName = builder.or(
              builder.like(builder.lower(teacherJoin.get("firstName")), "%" + teacherFirstName + "%"),
              builder.like(teacherJoin.get("firstName"), "%" + teacherFirstName + "%")
      );

      Predicate hasCourseTeacherLastName = builder.or(
              builder.like(builder.lower(teacherJoin.get("lastName")), "%" + teacherLastName + "%"),
              builder.like(teacherJoin.get("lastName"), "%" + teacherLastName + "%")
      );

    query
        .where(builder.and(hasCourseCode, hasCourseName, hasCourseCredits, hasCourseTeacherFirstName, hasCourseTeacherLastName))
        .orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder));

    return entityManager.createQuery(query)
        .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }


}
