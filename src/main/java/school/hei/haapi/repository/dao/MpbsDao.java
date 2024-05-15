package school.hei.haapi.repository.dao;

import static school.hei.haapi.service.utils.InstantUtils.getCurrentMondayOfTheWeek;
import static school.hei.haapi.service.utils.InstantUtils.getCurrentSaturdayOfTheWeek;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Mpbs.Mpbs;

@Repository
@AllArgsConstructor
public class MpbsDao {
  private EntityManager entityManager;

  public List<Mpbs> findMpbsOfTheWeek() {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery query = builder.createQuery(Mpbs.class);
    Root<Mpbs> root = query.from(Mpbs.class);
    List<Predicate> predicates = new ArrayList<>();

    predicates.add(
        builder.and(
            builder.between(
                root.get("creationDatetime"),
                getCurrentMondayOfTheWeek(),
                getCurrentSaturdayOfTheWeek())));

    query.distinct(true).where(predicates.toArray(new Predicate[0]));

    return entityManager.createQuery(query).getResultList();
  }
}
