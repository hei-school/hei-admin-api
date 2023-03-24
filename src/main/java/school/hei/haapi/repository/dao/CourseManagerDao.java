package school.hei.haapi.repository.dao;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import school.hei.haapi.endpoint.rest.model.SortOrder;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.User;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Repository
@AllArgsConstructor
public class CourseManagerDao {
    private EntityManager entityManager;

    public List<Course> findByCriteria(
            String code,
            String name,
            Integer credits,
            String teacherFirstName,
            String teacherLastName,
            SortOrder creditsOrder,
            SortOrder codeOrder,
            Pageable pageable
    ) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Course> query = builder.createQuery(Course.class);

        Root<Course> root = query.from(Course.class);
        Join<Course, User> teacherJoin = root.join("mainTeacher", JoinType.INNER);

        List<Predicate> predicateList = new ArrayList<>();

        if (code != null) {
            predicateList.add(builder.or(
                    builder.like(builder.lower(root.get("code")), "%" + code.toLowerCase() + "%")
            ));
        }

        if (name != null) {
            predicateList.add(builder.or(
                    builder.like(builder.lower(root.get("name")), "%" + name.toLowerCase() + "%")
            ));
        }

        if (credits != null) {
            predicateList.add(builder.or(
                    builder.equal(root.get("credits"), credits)
            ));
        }

        if (teacherLastName != null) {
            predicateList.add(builder.or(
                    builder.like(builder.lower(teacherJoin.get("lastName")), "%" + teacherLastName.toLowerCase() + "%")
            ));
        }

        if (teacherFirstName != null) {
            predicateList.add(builder.or(
                    builder.like(builder.lower(teacherJoin.get("firstName")), "%" + teacherFirstName.toLowerCase() + "%")
            ));
        }

        Predicate[] restrictions = new Predicate[predicateList.size()];
        for (int i = 0; i < predicateList.size(); i++) {
            restrictions[i] = predicateList.get(i);
        }

        query
                .where(restrictions.length == 0 ? builder.and(restrictions) : builder.or(restrictions))
                .orderBy(
                        creditsOrder.equals(SortOrder.ASC) ? builder.asc(root.get("credits")) : builder.desc(root.get("credits")),
                        codeOrder.equals(SortOrder.ASC) ? builder.asc(root.get("code")) : builder.desc(root.get("code"))
                );

        return entityManager.createQuery(query)
                .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }


}
