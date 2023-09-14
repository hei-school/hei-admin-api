package school.hei.haapi.repository.dao;

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
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.User;

@Repository
@AllArgsConstructor
public class GroupFlowDao {
  private final EntityManager entityManager;

  public List<GroupFlow> findLastGroupFlowOfStudent(String studentId, String groupId) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery query = builder.createQuery(GroupFlow.class);
    Root<GroupFlow> groupFlowRoot = query.from(GroupFlow.class);
    Join<GroupFlow, User> studentJoin = groupFlowRoot.join("student", JoinType.LEFT);
    Join<GroupFlow, Group> groupJoin = groupFlowRoot.join("group", JoinType.LEFT);

    Expression studentIdExpression = studentJoin.get("id");
    Predicate withStudentId = builder.equal(studentIdExpression, studentId);

    Expression groupIdExpression = groupJoin.get("id");
    Predicate withGroupId = builder.equal(groupIdExpression, groupId);

    Expression orderByExpression = groupFlowRoot.get("flowDatetime");

    query
        .distinct(true)
        .orderBy(builder.desc(orderByExpression))
        .where(builder.and(
            withGroupId,
            withStudentId,
            builder.equal(
              groupFlowRoot.get("groupFlowType"),
              GroupFlow.group_flow_type.JOIN)
            )
        );

    return entityManager.createQuery(query)
        .getResultList();
  }
}
