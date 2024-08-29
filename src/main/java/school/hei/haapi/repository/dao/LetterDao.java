package school.hei.haapi.repository.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;
import school.hei.haapi.model.Comment;
import school.hei.haapi.model.Letter;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.LetterRepository;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.criteria.JoinType.INNER;
import static jakarta.persistence.criteria.JoinType.LEFT;

@Repository
@AllArgsConstructor
public class LetterDao {

    private final EntityManager entityManager;

    public List<Letter> findByCriteria(String ref, String studentRef, Pageable pageable) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Letter> query = builder.createQuery(Letter.class);
        Root<Letter> root = query.from(Letter.class);
        Join<Letter, User> userJoin = root.join("student", INNER);
        List<Predicate> predicates = new ArrayList<>();

        if (studentRef != null) {
            predicates.add(
                    builder.or(
                            builder.like(builder.lower(userJoin.get("ref")), "%" + studentRef + "%"),
                            builder.like(userJoin.get("ref"), "%" + studentRef + "%")));
        }

        if (ref != null) {
            predicates.add(
                    builder.or(
                            builder.like(builder.lower(root.get("ref")), "%" + ref + "%"),
                            builder.like(root.get("ref"), "%" + ref + "%")));
        }

        if (!predicates.isEmpty()) {
            query.where(predicates.toArray(new Predicate[0])).distinct(true);
        }

        query.orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder));

        return entityManager
                .createQuery(query)
                .setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }
}
