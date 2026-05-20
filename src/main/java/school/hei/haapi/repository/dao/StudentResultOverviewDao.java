package school.hei.haapi.repository.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.ResultOverviewStatus;
import school.hei.haapi.model.StudentResultOverview;

@Repository
@AllArgsConstructor
public class StudentResultOverviewDao {
  private EntityManager entityManager;

  public List<StudentResultOverview> filteryByCriteria(
      String promotionId, ResultOverviewStatus status, @NotNull Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<StudentResultOverview> query = builder.createQuery(StudentResultOverview.class);
    Root<StudentResultOverview> root = query.from(StudentResultOverview.class);
    List<Predicate> predicates = new ArrayList<>();

    if (promotionId != null) {
      predicates.add(builder.equal(root.get("promotion").get("id"), promotionId));
    }

    if (status != null) {
      predicates.add(builder.equal(root.get("status"), status));
    }

    query.where(predicates.toArray(new Predicate[0]));

    return entityManager
        .createQuery(query)
        .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }
}
