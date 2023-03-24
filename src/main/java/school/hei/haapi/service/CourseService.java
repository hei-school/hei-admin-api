package school.hei.haapi.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.OrderDirection;
import school.hei.haapi.endpoint.rest.model.User;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.repository.CourseRepository;


@Service
@AllArgsConstructor
public class CourseService {
  private final CourseRepository courseRepository;
  private final EntityManager entityManager;

  public List<Course> getAllCourses(PageFromOne page,
                                    BoundedPageSize pageSize, String code,
                                    String name, String teacherFirstName,
                                    String teacherLastName, Integer credits,
                                    OrderDirection creditsOrder,
                                    OrderDirection codeOrder) {
    List<Sort.Order> orders = new ArrayList<>();
    if (creditsOrder != null) {
      orders.add(new Sort.Order(Sort.Direction.valueOf(creditsOrder.getValue()), "credits"));
    }
    if(codeOrder != null){
      orders.add(new Sort.Order(Sort.Direction.valueOf(codeOrder.getValue()), "code"));
    }

    Pageable pageable = PageRequest.of((page == null ? 1 : page.getValue() - 1),
            pageSize == null ? 15 : pageSize.getValue(), Sort.by(orders));

    if (name == null && teacherFirstName == null && teacherLastName == null && credits == null) {
      return courseRepository.findAll(pageable).stream().collect(Collectors.toUnmodifiableList());
    }

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Course> criteriaQuery = criteriaBuilder.createQuery(Course.class);
    Root<Course> courseRoot = criteriaQuery.from(Course.class);
    Join<Course, User> root = courseRoot.join("teacher");

    Predicate havingFirstName = null;
    if (teacherFirstName != null) {
      havingFirstName = criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")),
              "%" + teacherFirstName.toLowerCase() + "%");
    }

    Predicate havingLastName = null;
    if (teacherLastName != null) {
      havingLastName = criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")),
              "%" + teacherLastName.toLowerCase() + "%");
    }
    if (havingFirstName != null && havingLastName != null) {
      criteriaQuery.where(criteriaBuilder.and(havingFirstName, havingLastName));
    } else if (havingFirstName != null) {
      criteriaQuery.where(havingFirstName);
    } else if (havingLastName != null) {
      criteriaQuery.where(havingLastName);
    }

    Predicate code1 = null;
    if (code != null) {
      code1 = criteriaBuilder.like(criteriaBuilder.lower(courseRoot.get("code")),
          "%" + code.toLowerCase() + "%");
      criteriaQuery.where(code1);
    }

    return entityManager.createQuery(criteriaQuery)
            .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
            .setMaxResults(pageable.getPageSize())
            .getResultList();
  }
}