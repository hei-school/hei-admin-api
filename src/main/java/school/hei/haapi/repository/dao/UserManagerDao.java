package school.hei.haapi.repository.dao;

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
import javax.persistence.criteria.Subquery;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.AwardedCourse;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.User;

@Repository
@AllArgsConstructor
public class UserManagerDao {
  private EntityManager entityManager;

  public List<User> findByGroupIdAndCriteria(
      User.Role role,
      String ref,
      String firstName,
      String lastName,
      Pageable pageable,
      User.Status status,
      User.Sex sex,
      String groupId) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<User> query = builder.createQuery(User.class);
    Root<User> userRoot = query.from(User.class);
    Join<User, GroupFlow> groupGroupFlowJoin = userRoot.join("groupFlows", JoinType.LEFT);
    Join<GroupFlow, Group> groupJoin = groupGroupFlowJoin.join("group", JoinType.LEFT);
    List<Predicate> predicates = new ArrayList<>();

    // Take only the actual student in group
    Subquery<String> maxFlowDatetimeSubquery = query.subquery(String.class);
    Root<GroupFlow> subqueryGfRoot = maxFlowDatetimeSubquery.from(GroupFlow.class);
    maxFlowDatetimeSubquery.select(
        builder.function("MAX", String.class, subqueryGfRoot.get("flowDatetime")));
    maxFlowDatetimeSubquery.where(
        builder.equal(subqueryGfRoot.get("student"), groupGroupFlowJoin.get("student")),
        builder.equal(subqueryGfRoot.get("group"), groupGroupFlowJoin.get("group")));

    predicates.add(
        builder.and(
            builder.equal(groupJoin.get("id"), groupId),
            builder.or(
                builder.equal(
                    groupGroupFlowJoin.get("groupFlowType"), GroupFlow.group_flow_type.JOIN),
                builder.and(
                    builder.equal(
                        groupGroupFlowJoin.get("groupFlowType"), GroupFlow.group_flow_type.LEAVE),
                    builder.equal(
                        groupGroupFlowJoin.get("flowDatetime"), maxFlowDatetimeSubquery)))));

    if (firstName != null && !firstName.isEmpty()) {
      predicates.add(
          builder.and(
              builder.or(
                  builder.like(builder.lower(userRoot.get("firstName")), "%" + firstName + "%"),
                  builder.like(userRoot.get("firstName"), "%" + firstName + "%"))));
    }

    if (ref != null && !ref.isEmpty()) {
      predicates.add(
          builder.and(
              builder.or(
                  builder.like(builder.lower(userRoot.get("ref")), "%" + ref + "%"),
                  builder.like(userRoot.get("ref"), "%" + ref + "%"))));
    }

    if (lastName != null && !lastName.isEmpty()) {
      predicates.add(
          builder.and(
              builder.or(
                  builder.like(builder.lower(userRoot.get("lastName")), "%" + lastName + "%"),
                  builder.like(userRoot.get("lastName"), "%" + lastName + "%"))));
    }

    predicates.add(builder.equal(userRoot.get("role"), role));

    if (status != null) {
      predicates.add(builder.and(builder.equal(userRoot.get("status"), status)));
    }

    if (sex != null) {
      predicates.add(builder.and(builder.equal(userRoot.get("sex"), sex)));
    }

    Expression groupIdExpression = groupJoin.get("id");
    predicates.add(builder.equal(groupIdExpression, groupId));

    query.distinct(true).where(predicates.toArray(new Predicate[0]));
    return entityManager
        .createQuery(query)
        .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }

  public List<User> findByCriteria(
      User.Role role,
      String ref,
      String firstName,
      String lastName,
      Pageable pageable,
      User.Status status,
      User.Sex sex) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<User> query = builder.createQuery(User.class);
    Root<User> root = query.from(User.class);
    Predicate predicate = builder.conjunction();

    Predicate hasUserRef =
        builder.or(
            builder.like(builder.lower(root.get("ref")), "%" + ref + "%"),
            builder.like(root.get("ref"), "%" + ref + "%"));

    Predicate hasUserFirstName =
        builder.or(
            builder.like(builder.lower(root.get("firstName")), "%" + firstName + "%"),
            builder.like(root.get("firstName"), "%" + firstName + "%"));

    Predicate hasUserLastName =
        builder.or(
            builder.like(builder.lower(root.get("lastName")), "%" + lastName + "%"),
            builder.like(root.get("lastName"), "%" + lastName + "%"));

    Predicate hasUserRole = builder.equal(root.get("role"), role);

    if (firstName != null && !firstName.isEmpty()) {
      predicate = builder.and(predicate, hasUserFirstName);
    }

    if (status != null) {
      predicate = builder.and(predicate, builder.equal(root.get("status"), status));
    }

    if (sex != null) {
      predicate = builder.and(predicate, builder.equal(root.get("sex"), sex));
    }

    predicate = builder.and(predicate, hasUserRole, hasUserRef, hasUserLastName);

    query.where(predicate).orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder));

    return entityManager
        .createQuery(query)
        .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }

  public List<User> findByLinkedCourse(User.Role role, String courseId, Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<User> query = builder.createQuery(User.class);
    Root<User> root = query.from(User.class);
    Join<User, AwardedCourse> awardedCourse = root.join("awardedCourses");
    Join<AwardedCourse, Course> course = awardedCourse.join("course");

    Predicate hasCourseId = builder.equal(course.get("id"), courseId);
    Predicate hasUserRole = builder.equal(root.get("role"), role);
    query
        .distinct(true)
        .where(builder.and(hasUserRole, hasCourseId))
        .orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder));

    return entityManager
        .createQuery(query)
        .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }
}
