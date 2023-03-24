package school.hei.haapi.repository.dao;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.User;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@AllArgsConstructor
public class CourseManagerDao {

    private EntityManager entityManager;

    public List<Course> getCourseByCriteria(String code, String name, Integer credits, String  teacher_first_name, String teacher_last_name, Pageable pageable){
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Course> query = builder.createQuery(Course.class);
        Root<Course> root = query.from(Course.class);

        Join<Course, User> teacherJoin = root.join("teacher", JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();
        if (code != null) {
            predicates.add(builder.like(builder.lower(root.get("code")), "%" + code.toLowerCase() + "%"));
        }
        if (name != null) {
            predicates.add(builder.like(builder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
        }
        if (credits != null) {
            predicates.add(builder.equal(root.get("credits"), credits));
        }
        if (teacher_first_name != null) {
            predicates.add(builder.like(builder.lower(teacherJoin.get("firstName")), "%" + teacher_first_name.toLowerCase() + "%"));
        }
        if (teacher_last_name != null) {
            predicates.add(builder.like(builder.lower(teacherJoin.get("lastName")), "%" + teacher_last_name.toLowerCase() + "%"));
        }
        Predicate[] queryExpression = new Predicate[predicates.size()];
        query
                .where(builder.and(predicates.toArray(queryExpression)));

        return entityManager.createQuery(query)
                .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }

}
