package school.hei.haapi.repository.dao;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.AwardedCourse;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.User;

@Repository
@AllArgsConstructor
public class AwardedCourseDao {
  private final EntityManager entityManager;

  public List<AwardedCourse> findByCriteria(String teacherId, String courseId, Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<AwardedCourse> query = builder.createQuery(AwardedCourse.class);
    Root<AwardedCourse> root = query.from(AwardedCourse.class);
    Join<AwardedCourse, User> teacher = root.join("mainTeacher", JoinType.LEFT);
    Join<AwardedCourse, Course> Courses = root.join("course", JoinType.LEFT);

    List<Predicate> predicates = new ArrayList<>();

    if (teacherId != null) {
      predicates.add(
          builder.or(
              builder.like(builder.lower(teacher.get("id")), "%" + teacherId + "%"),
              builder.like(teacher.get("id"), "%" + teacherId + "%")));
    }

    if (courseId != null) {
      predicates.add(
          builder.or(
              builder.like(builder.lower(Courses.get("id")), "%" + courseId + "%"),
              builder.like(Courses.get("id"), "%" + courseId + "%")));
    }

    query.where(builder.and(predicates.toArray(new Predicate[0]))).distinct(true);

    return entityManager
        .createQuery(query)
        .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }
}
