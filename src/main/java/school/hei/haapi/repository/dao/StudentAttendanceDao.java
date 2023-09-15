package school.hei.haapi.repository.dao;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.StudentAttendance;
import school.hei.haapi.model.User;

@Repository
@AllArgsConstructor
public class StudentAttendanceDao {
  private EntityManager entityManager;

  public List<StudentAttendance> findByStudentKeyWord(String studentKeyWord, Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery query = builder.createQuery(StudentAttendance.class);
    Root<StudentAttendance> studentAttendanceRoot = query.from(StudentAttendance.class);

    Join<StudentAttendance, User> root = studentAttendanceRoot.join("student", JoinType.LEFT);

    Predicate predicate = builder.conjunction();

    Predicate hasStudentRef =
        builder.or(
            builder.like(builder.lower(root.get("ref")), "%" + studentKeyWord.toLowerCase() + "%"),
            builder.like(root.get("ref"), "%" + studentKeyWord + "%")
        );

    Predicate hasStudentFirstname =
        builder.or(
          builder.like(builder.lower(root.get("firstname")), "%" + studentKeyWord.toLowerCase() + "%"),
          builder.like(root.get("firstname"), "%" + studentKeyWord + "%")
        );

    Predicate hastStudentLastname =
        builder.or(
            builder.like(builder.lower(root.get("lastname")), "%" + studentKeyWord.toLowerCase() + "%"),
            builder.like(root.get("lastname"), "%" + studentKeyWord + "%")
        );
    predicate = builder.and(hasStudentRef, hasStudentFirstname, hastStudentLastname);

    query
        .distinct(true)
        .where(predicate);


    return entityManager.createQuery(query)
        .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }
}
