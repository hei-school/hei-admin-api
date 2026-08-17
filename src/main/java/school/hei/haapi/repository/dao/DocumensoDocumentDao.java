package school.hei.haapi.repository.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.model.DocumensoDocument;
import school.hei.haapi.model.DocumensoDocumentStatus;

@Repository
@AllArgsConstructor
public class DocumensoDocumentDao {
  private final EntityManager entityManager;

  public List<DocumensoDocument> filterByCriteria(
      Collection<String> studentIds,
      StudentLevel level,
      DocumensoDocumentStatus status,
      Pageable pageable) {
    if (studentIds == null || studentIds.isEmpty()) {
      return List.of();
    }
    var builder = entityManager.getCriteriaBuilder();
    var query = builder.createQuery(DocumensoDocument.class);
    var root = query.from(DocumensoDocument.class);

    List<Predicate> predicates = new ArrayList<>();
    predicates.add(root.get("student").get("id").in(studentIds));

    if (level != null) {
      predicates.add(builder.equal(root.get("level"), level));
    }

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
