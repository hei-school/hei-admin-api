package school.hei.haapi.repository.dao;

import static jakarta.persistence.criteria.JoinType.LEFT;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupAttender;

@Repository
@AllArgsConstructor
public class GroupAttenderDao {
  private final EntityManager entityManager;

  public List<GroupAttender> findAllByGroupIdAndStudentCriteria(
      String groupId,
      String studentRef,
      String studentLastname,
      String studentFirstname,
      Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<GroupAttender> query = builder.createQuery(GroupAttender.class);
    Root<GroupAttender> root = query.from(GroupAttender.class);
    Join<GroupAttender, Group> groupJoin = root.join("group", LEFT);
    Join<GroupAttender, Student> studentJoin = root.join("student", LEFT);
    List<Predicate> predicates = new ArrayList<>();

    Expression<String> groupIdExpression = groupJoin.get("id");
    predicates.add(builder.equal(groupIdExpression, groupId));

    if (studentRef != null) {
      predicates.add(
          builder.and(
              builder.or(
                  builder.like(builder.lower(studentJoin.get("ref")), "%" + studentRef + "%"),
                  builder.like(studentJoin.get("ref"), "%" + studentRef + "%"))));
    }

    if (studentFirstname != null) {
      predicates.add(
          builder.and(
              builder.or(
                  builder.like(
                      builder.lower(studentJoin.get("firstName")), "%" + studentFirstname + "%"),
                  builder.like(studentJoin.get("firstName"), "%" + studentFirstname + "%"))));
    }

    if (studentLastname != null) {
      predicates.add(
          builder.and(
              builder.or(
                  builder.like(
                      builder.lower(studentJoin.get("lastName")), "%" + studentLastname + "%"),
                  builder.like(studentJoin.get("lastName"), "%" + studentLastname + "%"))));
    }

    query.distinct(true).where(predicates.toArray(new Predicate[0]));

    return entityManager
        .createQuery(query)
        .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }
}
