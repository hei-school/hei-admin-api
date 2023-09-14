package school.hei.haapi.repository.dao;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import school.hei.haapi.model.AwardedCourse;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseSession;
import school.hei.haapi.model.StudentAttendance;
import school.hei.haapi.model.User;
import school.hei.haapi.service.utils.InstantUtils;

@Repository
@AllArgsConstructor
public class StudentAttendanceDao {
  private EntityManager entityManager;

  public List<StudentAttendance> findByStudentKeyWordAndCourseSessionCriteria(
      String studentKeyword, Pageable pageable,
      List<String> courseIds, List<String> teachersIds,
      Instant from, Instant to
  ) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery query = builder.createQuery(StudentAttendance.class);
    Root<StudentAttendance> studentAttendanceRoot = query.from(StudentAttendance.class);
    Join<StudentAttendance, CourseSession> courseSessionJoin =
        studentAttendanceRoot.join("courseSession", JoinType.LEFT);
    Join<CourseSession, AwardedCourse> awardedCourseJoin =
        courseSessionJoin.join("awardedCourse", JoinType.LEFT);
    Join<AwardedCourse, Course> courseJoin =
        awardedCourseJoin.join("course", JoinType.LEFT);
    Join<StudentAttendance, User> userJoin =
        studentAttendanceRoot.join("student", JoinType.LEFT);
    Join<AwardedCourse, User> teacherJoin =
        awardedCourseJoin.join("mainTeacher", JoinType.LEFT);
    List<Predicate> predicates = new ArrayList<>();

    // sorting attendance by created_at ASC
    Expression studentAttendanceCreatedAtExpression = studentAttendanceRoot.get("createdAt");

    if (studentKeyword != null && !studentKeyword.isEmpty()) {
      predicates.add(
          builder.and(
              builder.or(
                  builder.or(
                      builder.like(builder.lower(userJoin.get("ref")),
                          "%" + studentKeyword.toLowerCase() + "%"),
                      builder.like(userJoin.get("ref"), "%" + studentKeyword + "%")
                  ),
                  builder.or(
                      builder.like(builder.lower(userJoin.get("firstName")),
                          "%" + studentKeyword.toLowerCase() + "%"),
                      builder.like(userJoin.get("firstName"), "%" + studentKeyword + "%")
                  ),
                  builder.or(
                      builder.like(builder.lower(userJoin.get("lastName")),
                          "%" + studentKeyword.toLowerCase() + "%"),
                      builder.like(userJoin.get("lastName"), "%" + studentKeyword + "%")
                  )
              )
          )
      );
    }

    if (courseIds != null && !courseIds.isEmpty()) {
      Expression<String> courseIdExpression = courseJoin.get("id");
      predicates.add(builder.and(courseIdExpression.in(courseIds)));
    }

    if (teachersIds != null && !teachersIds.isEmpty()) {
      Expression<String> teacherIdExpression = teacherJoin.get("id");
      predicates.add(builder.and(teacherIdExpression.in(teachersIds)));
    }

    switch (getFilterCase(from, to)) {
      case 1:
        predicates.add(
            builder.and(
                builder.or(
                    builder.greaterThanOrEqualTo(studentAttendanceRoot.get("createdAt"), from),
                    builder.isNull(studentAttendanceRoot.get("createdAt"))
                ),
                builder.greaterThanOrEqualTo(courseSessionJoin.get("begin"), from)
            )
        );
        break;
      case 2:
        predicates.add(
            builder.and(
                builder.or(
                    builder.lessThanOrEqualTo(studentAttendanceRoot.get("createdAt"), to),
                    builder.isNull(studentAttendanceRoot.get("createdAt"))
                ),
                builder.lessThanOrEqualTo(courseSessionJoin.get("begin"), to)
            )
        );
        break;
      case 3:
        predicates.add(
            builder.and(
                builder.or(
                    builder.between(studentAttendanceRoot.get("createdAt"), from, to),
                    builder.isNull(studentAttendanceRoot.get("createdAt"))
                ),
                builder.between(courseSessionJoin.get("begin"), from, to)
            )
        );
        break;
      case 4:
        predicates.add(
            builder.and(
                builder.or(
                    builder.between(studentAttendanceRoot.get("createdAt"),
                        InstantUtils.getCurrentMondayOfTheWeek(),
                        InstantUtils.getCurrentSaturdayOfTheWeek()
                    ),
                    builder.isNull(studentAttendanceRoot.get("createdAt"))
                ),
                builder.between(courseSessionJoin.get("begin"),
                    InstantUtils.getCurrentMondayOfTheWeek(),
                    InstantUtils.getCurrentSaturdayOfTheWeek()
                )
            )
        );
        break;
      default:
        // No specific case
        break;
    }

    query
        .distinct(true)
        .orderBy(builder.asc(studentAttendanceCreatedAtExpression))
        .where(predicates.toArray(new Predicate[0]));

    return entityManager.createQuery(query)
        .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }

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