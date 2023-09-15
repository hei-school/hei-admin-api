package school.hei.haapi.repository.dao;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.StudentAttendance;

@Repository
@AllArgsConstructor
public class StudentAttendanceDao {
  private EntityManager entityManager;

  public List<StudentAttendance> findByStudentKeyWord(String firstName, String lastName, String ref, Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery query = builder.createQuery(StudentAttendance.class);
    Root<StudentAttendance> root = query.from(StudentAttendance.class);
    Predicate predicate = builder.conjunction();

    Predicate hasStudentRef =
        builder.or(
            builder.like(builder.lower(root.get("ref")), "%" + ref + "%"),
            builder.like(root.get("ref"), "%" + ref + "%")
        );

    Predicate hasStudentFirstname =
        builder.or(
          builder.like(builder.lower(root.get("firstname")), "%" + firstName + "%"),
          builder.like(root.get("firstname"), "%" + firstName + "%")
        );

    Predicate hastStudentLastname =
        builder.or(
            builder.like(builder.lower(root.get("lastname")), "%" + lastName + "%"),
            builder.like(root.get("lastname"), "%" + lastName + "%")
        );
    predicate = builder.and(hasStudentRef, hasStudentFirstname, hastStudentLastname);

    return entityManager.createQuery(query)
        .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }
}
