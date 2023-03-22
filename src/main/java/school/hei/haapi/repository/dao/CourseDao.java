package school.hei.haapi.repository.dao;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import school.hei.haapi.endpoint.rest.model.Order;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.PageFromOne;

@Repository
@AllArgsConstructor
public class CourseDao {
  private EntityManager entityManager;
  private List<Sort.Order> orders(Order creditsOrder, Order codeOrder) {
    List<Sort.Order> list = new ArrayList<>();
    if (creditsOrder != null) {
      list.add(new Sort.Order(Sort.Direction.valueOf(creditsOrder.getValue()), "credits"));
    }
    if (codeOrder != null) {
      list.add(new Sort.Order(Sort.Direction.valueOf(codeOrder.getValue()), "code"));
    } else {
      list.add(defaultOrder());
    }
    return list;
  }

  public List<Course> getCoursesBycriteria(String code, String name, Integer credits,
      String teacherFirstname, String teacherLastname, PageFromOne page,
      BoundedPageSize pageSize, Order creditsOrder, Order codeOrder) {
    int pageValue = page == null ? 0 : page.getValue() - 1;
    int pageSizeValue = pageSize == null ? 50 : pageSize.getValue();

    List<Sort.Order> filterOrder = orders(creditsOrder, codeOrder);
    Pageable pageable = PageRequest.of(pageValue, pageSizeValue, Sort.by(filterOrder));

    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Course> query = builder.createQuery(Course.class);
    Root<Course> root = query.from(Course.class);

    List<Predicate> predicates = new ArrayList<>();
    predicates.add(builder.equal(root, root));
    if (code != null) {
      predicates.add(builder.like(builder.lower(root.get("code")), "%" + code.toLowerCase() + "%"));
    }
    if (name != null) {
      predicates.add(builder.like(builder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
    }
    if (credits != null) {
      predicates.add(builder.equal(root.get("credits"), credits));
    }
    if (teacherFirstname != null) {
      predicates.add(builder.like(builder.lower(root.get("mainTeacher")
          .get("firstName")), "%" + teacherFirstname.toLowerCase() + "%"));
    }
    if (teacherLastname != null) {
      predicates.add(builder.like(builder.lower(root.get("mainTeacher")
          .get("lastName")), "%" + teacherLastname.toLowerCase() + "%"));
    }
    Predicate[] arrays = new Predicate[predicates.size()];
    query
        .where(builder.and(predicates.toArray(arrays)))
        .orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder));
    return entityManager.createQuery(query)
        .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }

  private Sort.Order defaultOrder() {
    return new Sort.Order(Sort.Direction.ASC, "code");
  }
}
