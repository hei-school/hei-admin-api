package school.hei.haapi.repository.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.SmsCampaign;
import school.hei.haapi.model.SmsCampaignStatus;

@Repository
@AllArgsConstructor
public class SmsCampaignDao {
  private final EntityManager entityManager;

  public List<SmsCampaign> filterByCriteria(SmsCampaignStatus status, Pageable pageable) {
    var builder = entityManager.getCriteriaBuilder();
    var query = builder.createQuery(SmsCampaign.class);
    var root = query.from(SmsCampaign.class);

    List<Predicate> predicates = new ArrayList<>();
    if (status != null) {
      predicates.add(builder.equal(root.get("status"), status));
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
