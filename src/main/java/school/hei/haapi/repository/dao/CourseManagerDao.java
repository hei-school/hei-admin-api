package school.hei.haapi.repository.dao;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.User;


import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

@Repository
@AllArgsConstructor
public class CourseManagerDao {
    private EntityManager entityManager;

    public List<Course> findByCriteria(String code, String name, Integer credits, String teacher_first_name, String teacher_last_name, String creditsOrder, String codeOrder,
                                       Pageable pageable) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Course> query = builder.createQuery(Course.class);
        Root<Course> root = query.from(Course.class);
        Join<Object,Object> teacher = root.join("MainTeacher");

        Predicate hasCourseCode =
                builder.or(
                        builder.like(builder.lower(root.get("code")), "%" + code.toLowerCase() + "%"),
                        builder.like(root.get("code"), "%" + code + "%")
                );

        Predicate hasCourseName = name.length()==0?
                builder.or(
                        builder.like(builder.lower(root.get("name")), "%" + name.toLowerCase() + "%"),
                        builder.like(root.get("name"), "%" + name + "%"),
                        builder.isNull(root.get("name"))
                ):
                builder.or(
                        builder.like(builder.lower(root.get("name")), "%" + name.toLowerCase() + "%"),
                        builder.like(root.get("name"), "%" + name + "%")
                );

        Predicate hasTeacherFirstName =
                builder.or(
                        builder.like(builder.lower(teacher.get("firstName")), "%" + teacher_first_name.toLowerCase() + "%"),
                        builder.like(teacher.get("firstName"), "%" + teacher_first_name + "%")
                );
        Predicate hasTeacherLastName =
                builder.or(
                        builder.like(builder.lower(teacher.get("lastName")), "%" + teacher_last_name.toLowerCase() + "%"),
                        builder.like(teacher.get("lastName"), "%" + teacher_last_name + "%")
                );

        Predicate hasTeacherFirstNameAndHasTeacherLastName = null;
        hasTeacherFirstNameAndHasTeacherLastName = (teacher_first_name.length()>0 && teacher_last_name.length()>0)?
                builder.or(hasTeacherFirstName,hasTeacherLastName):
                builder.and(hasTeacherFirstName,hasTeacherLastName);

        Predicate hasCourseCredits = builder.equal(root.get("credits"), credits);

        if (credits==null){
            query
                    .where(builder.and(hasCourseCode,hasCourseName,hasTeacherFirstNameAndHasTeacherLastName,hasTeacherLastName))
                    .orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder));
        }else {
            query
                    .where(builder.and(hasCourseCode,hasCourseName,hasTeacherFirstNameAndHasTeacherLastName,hasCourseCredits))
                    .orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder));
        }
        return entityManager.createQuery(query)
                .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }
}
