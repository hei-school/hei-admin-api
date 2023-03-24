package school.hei.haapi.repository.dao;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.User;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

@Repository
@AllArgsConstructor
public class CourseManagerDao {
  private EntityManager entityManager;

  public List<Course> findByCriteria(User.Role role , String firstName , String lastName , Pageable pageable){
      CriteriaBuilder builder = entityManager.getCriteriaBuilder();
      CriteriaQuery<Course> query = builder.createQuery(Course.class);
      Root<Course> root = query.from(Course.class);

      Predicate hasTeacherLastName =
              builder.or(
                      builder.like(builder.lower(root.get("lastName")), "%" + lastName + "%"),
                      builder.like(root.get("lastName"), "%" + lastName + "%")
              );

      Predicate hasTeacherFirstName =
              builder.or(
                      builder.like(builder.lower(root.get("firstName")), "%" + firstName + "%"),
                      builder.like(root.get("firstName"), "%" + firstName + "%")
              );

      Predicate hasUserRole =
              builder.or(
                      builder.equal(root.get("role"), role)
              );

      query
              .where(builder.or(hasUserRole ,hasTeacherFirstName, hasTeacherLastName))
              .orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder));

      return entityManager.createQuery(query)
              .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
              .setMaxResults(pageable.getPageSize())
              .getResultList();
  }
}
