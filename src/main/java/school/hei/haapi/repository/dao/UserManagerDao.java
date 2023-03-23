package school.hei.haapi.repository.dao;

import javax.persistence.criteria.Join;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.model.User;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

@Repository
@AllArgsConstructor
public class UserManagerDao {
    private EntityManager entityManager;

    public List<User> findByCriteria(User.Role role, String ref, String firstName, String lastName,
                                     Pageable pageable) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = builder.createQuery(User.class);
        Root<User> root = query.from(User.class);

        Predicate hasUserRef =
                builder.or(
                        builder.like(builder.lower(root.get("ref")), "%" + ref + "%"),
                        builder.like(root.get("ref"), "%" + ref + "%")
                );

        Predicate hasUserFirstName =
                builder.or(
                        builder.like(builder.lower(root.get("firstName")), "%" + firstName + "%"),
                        builder.like(root.get("firstName"), "%" + firstName + "%")
                );

        Predicate hasUserLastName =
                builder.or(
                        builder.like(builder.lower(root.get("lastName")), "%" + lastName + "%"),
                        builder.like(root.get("lastName"), "%" + lastName + "%")
                );

        Predicate hasUserRole =
                builder.or(
                        builder.equal(root.get("role"), role)
                );

        query
                .where(builder.and(hasUserRole, hasUserRef, hasUserFirstName, hasUserLastName))
                .orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder));


        return entityManager.createQuery(query)
                .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }

    //TODO : handle promotions by date intervals
    public List<User> findByPromotionRange(User.Role role, Pageable pageable,
                                           String ref, String firstName, String lastName,
                                           String promotionRange) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = builder.createQuery(User.class);
        Root<User> root = query.from(User.class);
        Join<User, Promotion> promotionJoin = root.join("promotions");

        Predicate hasUserRole =
                builder.or(
                        builder.equal(root.get("role"), role)
                );

        Predicate hasPromotionRange =
                builder.like(builder.lower(
                        promotionJoin.get("promotionRange")), "%" + promotionRange.toLowerCase() + "%");

        Predicate hasUserRef =
                builder.or(
                        builder.like(builder.lower(root.get("ref")), "%" + ref + "%"),
                        builder.like(root.get("ref"), "%" + ref + "%")
                );

        Predicate hasUserFirstName =
                builder.or(
                        builder.like(builder.lower(root.get("firstName")), "%" + firstName + "%"),
                        builder.like(root.get("firstName"), "%" + firstName + "%")
                );

        Predicate hasUserLastName =
                builder.or(
                        builder.like(builder.lower(root.get("lastName")), "%" + lastName + "%"),
                        builder.like(root.get("lastName"), "%" + lastName + "%")
                );

        query
                .where(builder.and(hasUserRole, hasUserRef, hasUserFirstName, hasUserLastName,
                        hasPromotionRange))
                .orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder));


        return entityManager.createQuery(query)
                .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }


}
