package school.hei.haapi.repository.dao;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.AwardedCourse;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.User;

@Repository
@AllArgsConstructor
public class UserManagerDao {
    private EntityManager entityManager;

    public List<User> findByCriteria(User.Role role, String ref, String firstName, String lastName,
                                     Pageable pageable) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = builder.createQuery(User.class);
        Root<User> root = query.from(User.class);
        Predicate predicate = builder.conjunction();

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

        Predicate hasUserRole = builder.equal(root.get("role"), role);

        if (firstName != null && !firstName.isEmpty()) {
            predicate = builder.and(predicate, hasUserFirstName);
        }

        predicate = builder.and(predicate, hasUserRole, hasUserRef, hasUserLastName);

        query
                .where(predicate)
                .orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder));

        return entityManager.createQuery(query)
                .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }

    public List<User> findByLinkedCourse(User.Role role, String courseId, Pageable pageable) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = builder.createQuery(User.class);
        Root<User> root = query.from(User.class);
        Join<User, AwardedCourse> awardedCourse = root.join("awardedCourses");
        Join<AwardedCourse, Course> course = awardedCourse.join("course");

        Predicate hasCourseId = builder.equal(course.get("id"), courseId);
        Predicate hasUserRole = builder.equal(root.get("role"), role);
        query
                .distinct(true)
                .where(builder.and(hasUserRole, hasCourseId))
                .orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder));

        return entityManager.createQuery(query)
                .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }
}
