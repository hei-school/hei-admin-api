package school.hei.haapi.repository.dao;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Courses;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

@Repository
@AllArgsConstructor
public class CourseManagerDao {
    private EntityManager entityManager;

    public List<Courses> findByFiltre(String code, String name, Integer credits, String teacher_first_name, String teacher_last_name, Pageable pageable) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Courses> query = builder.createQuery(Courses.class);
        Root<Courses> root = query.from(Courses.class);

        Predicate hasCourseCode =
                builder.or(
                        builder.like(builder.lower(root.get("code")), "%" + code + "%"),
                        builder.like(root.get("code"), "%" + code + "%")
                );
        Predicate hasCourseName =
                builder.or(
                        builder.like(builder.lower(root.get("name")), "%" + name + "%"),
                        builder.like(root.get("name"), "%" + name + "%")
                );
        Predicate hasCourseCredit =
                builder.or(
                        builder.like(builder.lower(root.get("credits")), "%" + credits + "%"),
                        builder.like(root.get("credits"), "%" + credits + "%")
                );
        Predicate hasCourseTeacher_first_name =
                builder.or(
                        builder.like(builder.lower(root.get("teacher_first_name")), "%" + teacher_first_name + "%"),
                        builder.like(root.get("teacher_first_name"), "%" + teacher_first_name + "%")
                );
        Predicate hasCourseTeacher_last_name =
                builder.or(
                        builder.like(builder.lower(root.get("teacher_last_name")), "%" + teacher_last_name + "%"),
                        builder.like(root.get("teacher_last_name"), "%" + teacher_last_name + "%")
                );
        query
                .where(builder.and(hasCourseCode, hasCourseName, hasCourseCredit, hasCourseTeacher_first_name, hasCourseTeacher_last_name))
                .orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder));
        return entityManager.createQuery(query)
                .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }

}
