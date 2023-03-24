package school.hei.haapi.repository.dao;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
public class CourseDao {
  private EntityManager entityManager;
  public List<Course> findAllOrderedBy(String orderByCode, String orderByCredit, Sort.Direction direction1, Sort.Direction direction2,
                                       Pageable pageable) {
          CriteriaBuilder builder = entityManager.getCriteriaBuilder();
          CriteriaQuery<Course> query = builder.createQuery(Course.class);
          Root<Course> root = query.from(Course.class);
          query.orderBy(
                  direction1.equals(Sort.Direction.ASC) ? builder.asc(root.get(orderByCode)) : builder.desc(root.get(orderByCode)),
                  direction2.equals(Sort.Direction.ASC) ? builder.asc(root.get(orderByCredit)) : builder.desc(root.get(orderByCredit))
          );
      query.orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder));

          return entityManager.createQuery(query)
                  .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
                  .setMaxResults(pageable.getPageSize())
                  .getResultList();
  }
}
