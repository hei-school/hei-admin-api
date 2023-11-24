package school.hei.haapi.repository.dao;

import javax.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class GroupFlowDao {
  private final EntityManager entityManager;

  //  public List<GroupFlow> findLastGroupFlowOfStudent(String studentId, String groupId) {
  //    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
  //    CriteriaQuery query = builder.createQuery(GroupFlow.class);
  //    Root<GroupFlow> groupFlowRoot = query.from(GroupFlow.class);
  //    Join<GroupFlow, User> studentJoin = groupFlowRoot.join("student", JoinType.LEFT);
  //    Join<GroupFlow, Group> groupJoin = groupFlowRoot.join("group", JoinType.LEFT);
  //
  //    Expression studentIdExpression = studentJoin.get("id");
  //    Predicate withStudentId = builder.equal(studentIdExpression, studentId);
  //
  //    Expression groupIdExpression = groupJoin.get("id");
  //    Predicate withGroupId = builder.equal(groupIdExpression, groupId);
  //
  //    Expression orderByExpression = groupFlowRoot.get("flowDatetime");
  //
  //    query
  //        .distinct(true)
  //        .orderBy(builder.desc(orderByExpression))
  //        .where(builder.and(
  //            withGroupId,
  //            withStudentId,
  //            builder.equal(
  //              groupFlowRoot.get("groupFlowType"),
  //              GroupFlow.group_flow_type.JOIN)
  //            )
  //        );
  //
  //    return entityManager.createQuery(query)
  //        .getResultList();
  //  }
}
