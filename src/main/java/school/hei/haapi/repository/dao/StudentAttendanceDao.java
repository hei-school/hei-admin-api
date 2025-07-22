package school.hei.haapi.repository.dao;

import static jakarta.persistence.criteria.JoinType.LEFT;
import static school.hei.haapi.service.utils.InstantUtils.currentMondayOfTheWeek;
import static school.hei.haapi.service.utils.InstantUtils.currentSaturdayOfTheWeekOrNext;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.CourseSession;
import school.hei.haapi.model.StudentAttendance;
import school.hei.haapi.model.User;

@Repository
@AllArgsConstructor
public class StudentAttendanceDao {
  private EntityManager entityManager;

  public List<StudentAttendance> findByStudentKeyWordAndCourseSessionCriteria(
      String studentKeyword,
      Pageable pageable,
      List<String> courseIds,
      List<String> teachersIds,
      Instant from,
      Instant to) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery query = builder.createQuery(StudentAttendance.class);
    Root<StudentAttendance> studentAttendanceRoot = query.from(StudentAttendance.class);
    Join<StudentAttendance, CourseSession> courseSessionJoin =
        studentAttendanceRoot.join("courseSession", LEFT);
    Join<CourseSession, CourseAssignment> awardedCourseJoin =
        courseSessionJoin.join("courseAssignment", LEFT);
    Join<CourseAssignment, Course> courseJoin = awardedCourseJoin.join("course", LEFT);
    Join<StudentAttendance, User> userJoin = studentAttendanceRoot.join("student", LEFT);
    Join<CourseAssignment, User> teacherJoin = awardedCourseJoin.join("mainTeacher", LEFT);
    List<Predicate> predicates = new ArrayList<>();

    // TODO: refactor this as it is very repetitive
    if (studentKeyword != null && !studentKeyword.isEmpty()) {
      predicates.add(
          builder.and(
              builder.or(
                  builder.or(
                      builder.like(
                          builder.lower(userJoin.get("ref")),
                          "%" + studentKeyword.toLowerCase() + "%"),
                      builder.like(userJoin.get("ref"), "%" + studentKeyword + "%")),
                  builder.or(
                      builder.like(
                          builder.lower(userJoin.get("firstName")),
                          "%" + studentKeyword.toLowerCase() + "%"),
                      builder.like(userJoin.get("firstName"), "%" + studentKeyword + "%")),
                  builder.or(
                      builder.like(
                          builder.lower(userJoin.get("lastName")),
                          "%" + studentKeyword.toLowerCase() + "%"),
                      builder.like(userJoin.get("lastName"), "%" + studentKeyword + "%")))));
    }

    if (courseIds != null && !courseIds.isEmpty()) {
      Expression<String> courseIdExpression = courseJoin.get("id");
      predicates.add(builder.and(courseIdExpression.in(courseIds)));
    }

    if (teachersIds != null && !teachersIds.isEmpty()) {
      Expression<String> teacherIdExpression = teacherJoin.get("id");
      predicates.add(builder.and(teacherIdExpression.in(teachersIds)));
    }

    // TODO: refactor this as it is very verbose
    switch (getFilterCase(from, to)) {
      case 1:
        predicates.add(
            builder.and(
                builder.or(
                    builder.greaterThanOrEqualTo(studentAttendanceRoot.get("createdAt"), from),
                    builder.isNull(studentAttendanceRoot.get("createdAt"))),
                builder.or(
                    builder.greaterThanOrEqualTo(courseSessionJoin.get("begin"), from),
                    builder.isNull(studentAttendanceRoot.get("courseSession")))));
        break;
      case 2:
        predicates.add(
            builder.and(
                builder.or(
                    builder.lessThanOrEqualTo(studentAttendanceRoot.get("createdAt"), to),
                    builder.isNull(studentAttendanceRoot.get("createdAt"))),
                builder.or(
                    builder.lessThanOrEqualTo(courseSessionJoin.get("begin"), to),
                    builder.isNull(studentAttendanceRoot.get("courseSession")))));
        break;
      case 3:
        predicates.add(
            builder.and(
                builder.or(
                    builder.between(studentAttendanceRoot.get("createdAt"), from, to),
                    builder.isNull(studentAttendanceRoot.get("createdAt"))),
                builder.or(
                    builder.between(courseSessionJoin.get("begin"), from, to),
                    builder.isNull(studentAttendanceRoot.get("courseSession")))));
        break;
      case 4:
        predicates.add(
            builder.and(
                builder.or(
                    builder.between(
                        studentAttendanceRoot.get("createdAt"),
                        currentMondayOfTheWeek(),
                        currentSaturdayOfTheWeekOrNext()),
                    builder.isNull(studentAttendanceRoot.get("createdAt"))),
                builder.or(
                    builder.between(
                        courseSessionJoin.get("begin"),
                        currentMondayOfTheWeek(),
                        currentSaturdayOfTheWeekOrNext()),
                    builder.isNull(studentAttendanceRoot.get("courseSession")))));
        break;
      default:
        // No specific case
        break;
    }

    query
        .distinct(true)
        .orderBy(builder.asc(studentAttendanceRoot.get("createdAt")))
        .where(predicates.toArray(new Predicate[0]));

    return entityManager
        .createQuery(query)
        .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }

  // TODO: should refactor this as it is counter-intuitive
  private int getFilterCase(Instant from, Instant to) {
    if (to == null && from == null) {
      return 4;
    }
    if (to != null && from != null) {
      return 3;
    }
    if (from != null) {
      return 1;
    }
    if (to != null) {
      return 2;
    }
    return 0;
  }
}
