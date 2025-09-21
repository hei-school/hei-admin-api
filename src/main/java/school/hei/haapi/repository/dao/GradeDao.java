package school.hei.haapi.repository.dao;

import static jakarta.persistence.criteria.JoinType.INNER;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Grade;

@Repository
@AllArgsConstructor
public class GradeDao {
  private final EntityManager entityManager;

  public List<Grade> getGradesByExamId(String examId, @NotNull Pageable pageable) {
    return getGradesByExamId(examId, null, pageable);
  }

  public List<Grade> getGradesByExamId(
      String examId, String studentRef, @NotNull Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Grade> query = builder.createQuery(Grade.class);
    Root<Grade> root = query.from(Grade.class);
    List<Predicate> predicates = new ArrayList<>();

    predicates.add(builder.equal(root.get("exam").get("id"), examId));
    if (studentRef != null) {
      predicates.add(builder.equal(root.get("student").get("ref"), studentRef));
    }

    query.where(predicates.toArray(new Predicate[0]));
    if (pageable.isUnpaged()) {
      return entityManager.createQuery(query).getResultList();
    }

    return entityManager
        .createQuery(query)
        .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }

  public List<Grade> getGradesByExamId(String examId) {
    return getGradesByExamId(examId, Pageable.unpaged());
  }

  public List<Grade> getStudentGradesByCourseId(String courseId, String studentId) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Grade> query = builder.createQuery(Grade.class);
    Root<Grade> gradeRoot = query.from(Grade.class);

    Join<Grade, Exam> examJoin = gradeRoot.join("exam", INNER);
    Join<Exam, CourseAssignment> courseAssignmentJoin = examJoin.join("courseAssignment", INNER);
    Join<CourseAssignment, Course> courseJoin = courseAssignmentJoin.join("course", INNER);

    query.where(
        builder.and(
            builder.equal(courseJoin.get("id"), courseId),
            builder.equal(gradeRoot.get("student").get("id"), studentId)));

    return entityManager.createQuery(query).getResultList();
  }
}
