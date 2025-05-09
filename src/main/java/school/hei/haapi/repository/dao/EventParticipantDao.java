package school.hei.haapi.repository.dao;

import static jakarta.persistence.criteria.JoinType.LEFT;
import static school.hei.haapi.service.utils.DateUtils.RangedInstant;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import school.hei.haapi.endpoint.rest.model.AttendanceStatus;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.User;

@Repository
@AllArgsConstructor
public class EventParticipantDao {
  private final EntityManager entityManager;

  public List<EventParticipant> findByCriteria(
      String eventId,
      Pageable pageable,
      String groupRef,
      String name,
      String ref,
      AttendanceStatus attendanceStatus,
      RangedInstant eventBeginRange) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<EventParticipant> query = builder.createQuery(EventParticipant.class);
    Root<EventParticipant> root = query.from(EventParticipant.class);

    List<Predicate> predicates =
        getPredicates(
            builder, root, eventId, groupRef, name, ref, attendanceStatus, eventBeginRange);

    if (!predicates.isEmpty()) {
      query.where(predicates.toArray(new Predicate[0]));
    }

    if (pageable != null) {
      query.orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder));

      return entityManager
          .createQuery(query)
          .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
          .setMaxResults(pageable.getPageSize())
          .getResultList();
    }

    return entityManager.createQuery(query).getResultList();
  }

  private List<Predicate> getPredicates(
      CriteriaBuilder builder,
      Root<EventParticipant> root,
      String eventId,
      String groupRef,
      String name,
      String ref,
      AttendanceStatus attendanceStatus,
      RangedInstant eventBeginRange) {
    List<Predicate> predicates = new ArrayList<>();

    if (eventId != null) {
      Join<EventParticipant, Event> eventJoin = root.join("event", LEFT);
      predicates.add(builder.equal(eventJoin.get("id"), eventId));
    }

    if (groupRef != null) {
      Join<EventParticipant, Group> groupJoin = root.join("group", LEFT);
      predicates.add(builder.equal(groupJoin.get("ref"), groupRef));
    }

    if (name != null || ref != null) {
      Join<EventParticipant, User> userJoin = root.join("participant", LEFT);
      if (name != null) {
        predicates.add(
            builder.and(
                builder.or(
                    builder.or(
                        builder.like(
                            builder.lower(userJoin.get("firstName")),
                            "%" + name.toLowerCase() + "%"),
                        builder.like(userJoin.get("firstName"), "%" + name + "%")),
                    builder.or(
                        builder.like(
                            builder.lower(userJoin.get("lastName")),
                            "%" + name.toLowerCase() + "%"),
                        builder.like(userJoin.get("lastName"), "%" + name + "%")))));
      }

      if (ref != null) {
        predicates.add(
            builder.like(builder.lower(userJoin.get("ref")), "%" + ref.toLowerCase() + "%"));
      }
    }

    if (attendanceStatus != null) {
      predicates.add(builder.equal(root.get("status"), attendanceStatus));
    }

    Path<Instant> eventBeginDateTime = root.get("event").get(Event.BEGIN_DATETIME);
    predicates.add(builder.isNotNull(eventBeginDateTime));
    if (eventBeginRange != null) {
      if (eventBeginRange.from() != null) {
        predicates.add(builder.greaterThanOrEqualTo(eventBeginDateTime, eventBeginRange.from()));
      }
      if (eventBeginRange.to() != null) {
        predicates.add(builder.lessThanOrEqualTo(eventBeginDateTime, eventBeginRange.to()));
      }
    }

    return predicates;
  }
}
