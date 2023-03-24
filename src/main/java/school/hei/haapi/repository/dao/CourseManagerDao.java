package school.hei.haapi.repository.dao;

import java.util.ArrayList;
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
import school.hei.haapi.model.Course;
import school.hei.haapi.model.User;

@AllArgsConstructor
@Repository
public class CourseManagerDao {
  private final EntityManager entityManager;

  public List<Course> findCoursesByCriteria(String code, String name, Integer credits, String teacherFirstName, String teacherLastName,
                                            Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Course> query = builder.createQuery(Course.class);
    Root<Course> root = query.from(Course.class);
    Join<Course, User> join = root.join("mainTeacher");
    List<Predicate> predicates = new ArrayList<>();
    
    Predicate[] predicatesArray = retrieveNotNullPredicates(code, name, credits, teacherFirstName, teacherLastName,
        builder, join, predicates);

    query
        .where(builder.and(predicates.toArray(predicatesArray)))
        .orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder));

    return entityManager.createQuery(query)
        .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }

  public Predicate[] retrieveNotNullPredicates(
      String code, String name, Integer credits, String teacherFirstName, String teacherLastName, CriteriaBuilder builder, Join<Course,
      User> join, List<Predicate> predicates
  ) {

    if (code != null) {
      predicates.add(builder.or(
          builder.like(root.get("code"), "%" + code + "%"),
          builder.like(builder.lower(root.get("code")), "%" + code + "%")
      ));
    }
    if (name != null) {
      predicates.add(builder.or(
          builder.like(root.get("name"), "%" + name + "%"),
          builder.like(builder.lower(root.get("name")), "%" + name + "%")
      ));
    }
    if (credits != null) {
      predicates.add(builder.equal(root.get("credits"), credits)
      );
    }
    if (teacherFirstName != null) {
      predicates.add(builder.or(
          builder.like(join.get("firstName"), "%" + teacherFirstName + "%"),
          builder.like(builder.lower(join.get("firstName")), "%" + teacherFirstName.toLowerCase() + "%")
      ));
    }
    if (teacherLastName != null) {
      predicates.add(builder.or(
          builder.like(join.get("lastName"), "%" + teacherLastName + "%"),
          builder.like(builder.lower(join.get("lastName")), "%" + teacherLastName.toLowerCase() + "%")
      ));
    }
    return new Predicate[predicates.size()];
  }
}
