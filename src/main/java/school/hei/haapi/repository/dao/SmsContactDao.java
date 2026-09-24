package school.hei.haapi.repository.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.SmsContact;
import school.hei.haapi.model.SmsContactOwnerRole;

@Repository
@AllArgsConstructor
public class SmsContactDao {
  private final EntityManager entityManager;

  public List<SmsContact> filterByCriteria(
      String contactGroupId, SmsContactOwnerRole ownerRole, Pageable pageable) {
    var builder = entityManager.getCriteriaBuilder();
    var query = builder.createQuery(SmsContact.class);
    var root = query.from(SmsContact.class);
    // Explicit select is required as soon as a second root (groupRoot, below) is added — Hibernate
    // 6 rejects a CriteriaQuery with multiple roots and no explicit select ("Criteria has multiple
    // query roots"), unlike a plain implicit cross join in older Hibernate versions.
    query.select(root);

    List<Predicate> predicates = new ArrayList<>();
    predicates.add(builder.isFalse(root.get("isDeleted")));

    if (contactGroupId != null) {
      var groupRoot = query.from(school.hei.haapi.model.SmsContactGroup.class);
      predicates.add(builder.equal(groupRoot.get("id"), contactGroupId));
      // Declared (not cast) as Expression<SmsContact> to pick the isMember(Expression, Expression)
      // overload — root itself is ambiguous between it and isMember(Object, Expression).
      Expression<SmsContact> rootAsExpression = root;
      predicates.add(
          builder.isMember(rootAsExpression, groupRoot.<List<SmsContact>>get("members")));
    }
    if (ownerRole != null) {
      predicates.add(builder.equal(root.get("ownerRole"), ownerRole));
    }

    query.where(predicates.toArray(new Predicate[0]));
    query.orderBy(builder.desc(root.get("creationDatetime")));

    return entityManager
        .createQuery(query)
        .setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }
}
