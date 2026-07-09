package school.hei.haapi.repository.dao;

import static jakarta.persistence.criteria.JoinType.LEFT;
import static school.hei.haapi.endpoint.rest.model.WorkStudyStatus.HAVE_BEEN_WORKING;
import static school.hei.haapi.endpoint.rest.model.WorkStudyStatus.WILL_BE_WORKING;
import static school.hei.haapi.endpoint.rest.model.WorkStudyStatus.WORKING;
import static school.hei.haapi.model.GroupFlow.GroupFlowType.JOIN;
import static school.hei.haapi.model.GroupFlow.GroupFlowType.LEAVE;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.endpoint.rest.model.WorkStudyStatus;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.User;
import school.hei.haapi.model.WorkDocument;
import school.hei.haapi.service.utils.CollectionUtils;

@Repository
@AllArgsConstructor
@Slf4j
public class UserManagerDao {
  private EntityManager entityManager;
  private CollectionUtils collectionUtils;

  // TODO: create UserManagerDaoTest
  public List<User> findByCriteria(
      User.Role role,
      String ref,
      String firstName,
      String lastName,
      @NonNull Pageable pageable,
      User.Status status,
      User.Sex sex,
      WorkStudyStatus workStatus,
      Instant commitmentBeginDate,
      String courseId,
      Instant commitmentComparison,
      Collection<String> excludeGroupIds,
      Collection<String> includeGroupIds) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<User> query = builder.createQuery(User.class);
    Root<User> root = query.from(User.class);
    Join<User, WorkDocument> workDocumentJoin = null;
    Predicate predicate = builder.conjunction();

    if (courseId != null && !courseId.isBlank()) {
      Join<User, CourseAssignment> courseAssignmentJoin = root.join("courseAssignments", LEFT);
      Join<CourseAssignment, Course> courseJoin = courseAssignmentJoin.join("course", LEFT);
      Expression<String> courseIdExpression = courseJoin.get("id");
      predicate = builder.and(predicate, builder.equal(courseIdExpression, courseId));
    }

    if (commitmentBeginDate != null) {
      workDocumentJoin = root.join("workDocuments", LEFT);
      Expression<Instant> commitmentBeginExpression = workDocumentJoin.get("commitmentBegin");
      predicate =
          builder.and(
              predicate,
              builder.greaterThanOrEqualTo(commitmentBeginExpression, commitmentBeginDate));
    }

    if (firstName != null) {
      predicate =
          builder.and(
              predicate,
              builder.or(
                  builder.like(builder.lower(root.get("firstName")), "%" + firstName + "%"),
                  builder.like(root.get("firstName"), "%" + firstName + "%")));
    }

    if (status != null) {
      predicate = builder.and(predicate, builder.equal(root.get("status"), status));
    }

    if (sex != null) {
      predicate = builder.and(predicate, builder.equal(root.get("sex"), sex));
    }

    if (WORKING.equals(workStatus)) {
      workDocumentJoin = root.join("workDocuments", LEFT);
      predicate =
          builder.and(
              predicate,
              builder.lessThanOrEqualTo(
                  workDocumentJoin.get("commitmentBegin"), commitmentComparison),
              builder.isNull(workDocumentJoin.get("commitmentEnd")),
              builder.greaterThan(workDocumentJoin.get("commitmentEnd"), commitmentComparison));
    }
    if (HAVE_BEEN_WORKING.equals(workStatus)) {
      workDocumentJoin = root.join("workDocuments", LEFT);
      predicate =
          builder.and(
              predicate,
              builder.lessThanOrEqualTo(
                  workDocumentJoin.get("commitmentEnd"), commitmentComparison));
    }
    if (WILL_BE_WORKING.equals(workStatus)) {
      workDocumentJoin = root.join("workDocuments", LEFT);
      predicate =
          builder.and(
              predicate,
              builder.greaterThanOrEqualTo(
                  workDocumentJoin.get("commitmentBegin"), commitmentComparison));
    }

    if (isListCriteriaPresent(excludeGroupIds)) {
      predicate =
          builder.and(
              predicate,
              builder.not(root.get("id").in(currentGroupQuery(excludeGroupIds, query, builder))));
    }

    if (isListCriteriaPresent(includeGroupIds)) {
      predicate =
          builder.and(
              predicate, root.get("id").in(currentGroupQuery(includeGroupIds, query, builder)));

      if (isListCriteriaPresent(excludeGroupIds)) {
        var commonElement = collectionUtils.findCommonElement(excludeGroupIds, includeGroupIds);
        if (!commonElement.isEmpty())
          log.warn(
              "Group filter conflict: same group(s) present in both include and exclude filters ->"
                  + " {}",
              commonElement);
      }
    }

    if (role != null) {
      predicate = builder.and(predicate, builder.equal(root.get("role"), role));
    }

    if (ref != null && !ref.isEmpty()) {
      predicate =
          builder.and(
              predicate,
              builder.or(
                  builder.like(builder.lower(root.get("ref")), "%" + ref + "%"),
                  builder.like(root.get("ref"), "%" + ref + "%")));
    }

    if (lastName != null) {
      predicate =
          builder.and(
              predicate,
              builder.or(
                  builder.like(builder.lower(root.get("lastName")), "%" + lastName + "%"),
                  builder.like(root.get("lastName"), "%" + lastName + "%")));
    }

    if (pageable.isUnpaged()) {
      query.where(predicate);
      return entityManager.createQuery(query).getResultList();
    }

    query.where(predicate).orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder));

    return entityManager
        .createQuery(query)
        .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }

  private Subquery<String> currentGroupQuery(
      @NonNull Collection<String> groupIds, CriteriaQuery<User> query, CriteriaBuilder builder) {
    Subquery<String> subquery = query.subquery(String.class);
    Root<GroupFlow> groupFlowRoot = subquery.from(GroupFlow.class);

    subquery.select(groupFlowRoot.get("student").get("id"));
    subquery.where(groupFlowRoot.get("group").get("id").in(groupIds));

    subquery.groupBy(groupFlowRoot.get("group").get("id"), groupFlowRoot.get("student").get("id"));

    Expression<Integer> joinCount =
        builder.sum(
            builder
                .<Integer>selectCase()
                .when(builder.equal(groupFlowRoot.get("groupFlowType"), JOIN), 1)
                .otherwise(0));

    Expression<Integer> leaveCount =
        builder.sum(
            builder
                .<Integer>selectCase()
                .when(builder.equal(groupFlowRoot.get("groupFlowType"), LEAVE), 1)
                .otherwise(0));

    subquery.having(builder.greaterThan(joinCount, leaveCount));
    return subquery;
  }

  @Transactional
  public void updateUserStatusById(User.Status status, String userId) {
    User user = entityManager.find(User.class, userId);
    if (user != null) {
      user.setStatus(status);
      entityManager.merge(user);
    }
  }

  private static <T> boolean isListCriteriaPresent(Collection<T> criteria) {
    return criteria != null && !criteria.isEmpty();
  }
}
