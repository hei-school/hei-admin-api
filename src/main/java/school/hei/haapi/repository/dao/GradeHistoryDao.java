package school.hei.haapi.repository.dao;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static school.hei.haapi.model.GradeChangeHistory.CHANGE_INSTANT;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.GradeChangeHistory;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.GradeService;

@AllArgsConstructor
@Component
public class GradeHistoryDao {
  private GradeService gradeService;
  private EntityManager entityManager;

  public List<GradeChangeHistory> findByCriteriaOrderedByChangeInstant(
      PageFromOne page,
      BoundedPageSize pageSize,
      String gradeId,
      Instant changeInstantFrom,
      Instant changeInstantTo,
      String comment) {
    return findByCriteriaOrderedByChangeInstant(
        PageRequest.of(page.getValue() - 1, pageSize.getValue()),
        gradeId,
        changeInstantFrom,
        changeInstantTo,
        comment);
  }

  public List<GradeChangeHistory> findByCriteriaOrderedByChangeInstant(
      Pageable pageable,
      String gradeId,
      Instant changeInstantFrom,
      Instant changeInstantTo,
      String comment) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<GradeChangeHistory> query = builder.createQuery(GradeChangeHistory.class);
    Root<GradeChangeHistory> root = query.from(GradeChangeHistory.class);
    ArrayList<Predicate> predicates = new ArrayList<>();

    if (gradeId != null) {
      gradeService.getById(gradeId);
      predicates.add(builder.equal(root.get("grade").get("id"), gradeId));
    }

    if (changeInstantFrom != null) {
      predicates.add(builder.greaterThanOrEqualTo(root.get(CHANGE_INSTANT), changeInstantFrom));
    }

    if (changeInstantTo != null) {
      predicates.add(builder.lessThanOrEqualTo(root.get(CHANGE_INSTANT), changeInstantTo));
    }

    if (comment != null) {
      predicates.add(
          builder.like(builder.lower(root.get("comment")), "%" + comment.toLowerCase() + "%"));
    }

    query
        .where(predicates.toArray(new Predicate[0]))
        .orderBy(QueryUtils.toOrders(Sort.by(ASC, CHANGE_INSTANT), root, builder));

    return entityManager
        .createQuery(query)
        .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }
}
