package school.hei.haapi.repository.dao;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@AllArgsConstructor
public class CourseDao {
  private EntityManager entityManager;

  public List<Course> findAllOrderedBy(String orderByCode, String orderByCredit, Sort.Direction direction1, Sort.Direction direction2,
                                       String name, String code, Integer credits, String teacherFirstName, String teacherLastName,
                                       Pageable pageable) {
          CriteriaBuilder builder = entityManager.getCriteriaBuilder();
          CriteriaQuery<Course> query = builder.createQuery(Course.class);
          Root<Course> root = query.from(Course.class);
          Root<Course> course = query.from(Course.class);
          query.orderBy(
                  direction1.equals(Sort.Direction.ASC) ? builder.asc(root.get(orderByCode)) : builder.desc(root.get(orderByCode)),
                  direction2.equals(Sort.Direction.ASC) ? builder.asc(root.get(orderByCredit)) : builder.desc(root.get(orderByCredit))
          );
      List<Predicate> predicates = new ArrayList<>();

      if (name != null && !name.isEmpty()) {
          predicates.add(builder.like(builder.lower(course.get("name")), "%" + name.toLowerCase() + "%"));
      }

      if (code != null && !code.isEmpty()) {
          predicates.add(builder.like(builder.lower(course.get("code")), "%" + code.toLowerCase() + "%"));
      }

      if (credits != null) {
          predicates.add(builder.equal(course.get("credits"), credits));
      }

      if (teacherFirstName != null && !teacherFirstName.isEmpty()) {
          Join<Course, User> teacherJoin = course.join("mainTeacher");
          predicates.add(builder.like(builder.lower(teacherJoin.get("firstName")), "%" + teacherFirstName.toLowerCase() + "%"));
      }

      if (teacherLastName != null && !teacherLastName.isEmpty()) {
          Join<Course, User> teacherJoin = course.join("mainTeacher");
          predicates.add(builder.like(builder.lower(teacherJoin.get("lastName")), "%" + teacherLastName.toLowerCase() + "%"));
      }

      query.where(builder.and(predicates.toArray(new Predicate[0])));
      query.orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder));

          return entityManager.createQuery(query)
                  .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
                  .setMaxResults(pageable.getPageSize())
                  .getResultList();
  }


}
