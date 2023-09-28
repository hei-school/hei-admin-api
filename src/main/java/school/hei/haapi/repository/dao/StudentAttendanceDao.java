package school.hei.haapi.repository.dao;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.CourseSession;
import school.hei.haapi.model.StudentAttendance;
import school.hei.haapi.model.User;

@Repository
@AllArgsConstructor
public class StudentAttendanceDao {
  private EntityManager entityManager;

  public List<StudentAttendance> findByStudentKeyWordAndCourseSessionCriteria(
      String studentKeyword, Pageable pageable,
      List<String>courseIds, List<String>teachersIds, Instant from, Instant to
  ) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery query = builder.createQuery(StudentAttendance.class);
    Root<StudentAttendance> studentAttendanceRoot = query.from(StudentAttendance.class);
    Join<StudentAttendance, CourseSession> courseSessionJoin = studentAttendanceRoot.join("courseSession", JoinType.LEFT);
    Join<StudentAttendance, User> userJoin = studentAttendanceRoot.join("student", JoinType.LEFT);
    List<Predicate> predicates = new ArrayList<>();

    predicates.add(
        builder.or(
            builder.like(builder.lower(userJoin.get("ref")), "%" + studentKeyword.toLowerCase() + "%"),
            builder.like(userJoin.get("ref"), "%" + studentKeyword + "%")
        )
    );

    predicates.add(
        builder.or(
            builder.like(builder.lower(userJoin.get("firstName")), "%" + studentKeyword.toLowerCase() + "%"),
            builder.like(userJoin.get("firstName"), "%" + studentKeyword + "%")
        )
    );

    predicates.add(
        builder.or(
            builder.like(builder.lower(userJoin.get("lastName")), "%" + studentKeyword.toLowerCase() + "%"),
            builder.like(userJoin.get("lastName"), "%" + studentKeyword + "%")
        )
    );

    switch (getFilterCase(courseIds, teachersIds, from, to, studentKeyword)) {
      case 1:
        Expression<String> courseSessionIdExpression = courseSessionJoin.get("course").get("id");
        predicates.add(builder.and(courseSessionIdExpression.in(courseIds)));
        break;
      case 2:
        predicates.add(builder.and(
            builder.greaterThanOrEqualTo(studentAttendanceRoot.get("createdAt"), from)));
        break;
      case 3:
        predicates.add(builder.and(
            builder.lessThanOrEqualTo(studentAttendanceRoot.get("createdAt"), to)));
        break;
      case 4:
        predicates.add(builder.and(
            builder.between(studentAttendanceRoot.get("createdAt"), from, to)));
        break;
      case 5:
        Expression<String> teacherIdExpression = courseSessionJoin.get("course").get("mainTeacher").get("id");
        predicates.add(builder.and(teacherIdExpression.in(teacherIdExpression)));
        break;
      case 6:
        predicates.add(
            builder.and(
                builder.between(studentAttendanceRoot.get("createdAt"),
                    LocalDateTime.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY)),
                    LocalDateTime.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)))
            )
        );
      default:
        // No specific case
        break;
    }

    query
        .distinct(true)
        .where(builder.and(predicates.toArray(new Predicate[0])));

    return entityManager.createQuery(query)
        .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }

  private int getFilterCase(List<String> coursesIds, List<String> teachersIds,
                            Instant from, Instant to, String studentKeyword
  ) {
    if (to == null && from == null) {
      return 6;
    }
    if (teachersIds != null && !teachersIds.isEmpty()) {
      return 5;
    }
    if (to != null && from != null) {
      return 4;
    }
    if (from != null) {
      return 2;
    }
    if (to != null) {
      return 3;
    }
    if (coursesIds != null && !coursesIds.isEmpty()) {
      return 1;
    }
    return 0;
  }
}