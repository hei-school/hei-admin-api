package school.hei.haapi.repository.dao;

import static jakarta.persistence.criteria.JoinType.INNER;
import static school.hei.haapi.model.GroupFlow.GroupFlowType.JOIN;
import static school.hei.haapi.model.GroupFlow.GroupFlowType.LEAVE;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.User;

@Repository
@AllArgsConstructor
public class GroupDao {
  private final EntityManager entityManager;

  public List<Group> findByCriteria(String ref, String studentRef, Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Group> query = builder.createQuery(Group.class);
    Root<Group> root = query.from(Group.class);

    List<Predicate> predicates = new ArrayList<>();

    if (ref != null) {
      predicates.add(
          builder.or(
              builder.like(builder.lower(root.get("ref")), "%" + ref + "%"),
              builder.like(root.get("ref"), "%" + ref + "%")));
    }

    if (studentRef != null) {
      Subquery<String> subquery = query.subquery(String.class);
      Root<GroupFlow> groupFlowRoot = subquery.from(GroupFlow.class);
      Join<GroupFlow, User> studentJoin = groupFlowRoot.join("student", INNER);

      subquery.select(groupFlowRoot.get("group").get("id"));
      subquery.where(builder.like(studentJoin.get("ref"), "%" + studentRef + "%"));
      subquery.groupBy(
          groupFlowRoot.get("group").get("id"), groupFlowRoot.get("student").get("id"));

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

      predicates.add(root.get("id").in(subquery));
    }

    query
        .orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder))
        .where(predicates.toArray(new Predicate[0]));
    ;

    return entityManager
        .createQuery(query)
        .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }
}
