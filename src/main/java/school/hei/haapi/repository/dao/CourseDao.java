package school.hei.haapi.repository.dao;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import school.hei.haapi.endpoint.rest.model.Teacher;
import school.hei.haapi.model.Course;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import school.hei.haapi.model.User;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

@Repository
@AllArgsConstructor
public class CourseDao {
    private EntityManager entityManager;

    public List<Course> findByCriteria(String code, String name, Integer credits, String teacherFirstName, String teacherLastName, Course.CreditsOrder creditsOrder, Course.CodeOrder codeOrder,
                                         Pageable pageable) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Course> query = builder.createQuery(Course.class);
        Root<Course> root = query.from(Course.class);
        Join<Course, User> teacherJoin = root.join("mainTeacher");


        Predicate hasCourseCode =
                builder.or(
                        builder.like(builder.lower(root.get("code")), "%" + code + "%"),
                        builder.like(root.get("code"), "%" + code + "%")
                );
        Order codeOrderClause;
        switch (codeOrder) {
            case ASC:
                codeOrderClause = builder.asc(root.get("code"));
                break;
            case DESC:
                codeOrderClause = builder.desc(root.get("code"));
                break;
            default:
                codeOrderClause = builder.asc(root.get("code"));
        }

        Predicate hasCourseName =
                builder.or(
                        builder.like(builder.lower(root.get("name")), "%" + name + "%"),
                        builder.like(root.get("name"), "%" + name + "%")
                );

        Predicate hasCredits =
                credits != null ?
                        builder.or(
                                builder.equal(root.get("credits"), credits)
                        ) : builder.conjunction();

        Order creditsOrderClause;
        switch (creditsOrder) {
            case ASC:
                creditsOrderClause = builder.asc(root.get("credits"));
                break;
            case DESC:
                creditsOrderClause = builder.desc(root.get("credits"));
                break;
            default:
                creditsOrderClause = builder.asc(root.get("credits"));
        }

        Predicate hasTeacherFirstName =
                builder.or(
                        builder.like(builder.lower(teacherJoin.get("firstName")), "%" + teacherFirstName.toLowerCase() + "%"),
                        builder.like(teacherJoin.get("firstName"), "%" + teacherFirstName.toLowerCase() + "%")
                );

        Predicate hasTeacherLastName =
                builder.or(
                        builder.like(builder.lower(teacherJoin.get("lastName")), "%" + teacherLastName.toLowerCase() + "%"),
                        builder.like(teacherJoin.get("lastName"), "%" + teacherLastName.toLowerCase() + "%")
                );

        Predicate hasTeacherFirstNameAndTeacherLastName;
        if(!teacherFirstName.isEmpty() && teacherLastName.isEmpty()){
            hasTeacherFirstNameAndTeacherLastName = hasTeacherFirstName;
        } else if(teacherFirstName.isEmpty() && !teacherLastName.isEmpty()){
            hasTeacherFirstNameAndTeacherLastName = hasTeacherFirstName;
        } else hasTeacherFirstNameAndTeacherLastName = builder.or(hasTeacherFirstName, hasTeacherLastName);

        query
                .where(builder.and(hasCourseCode, hasCourseName, hasCredits, hasTeacherFirstName, hasTeacherLastName))
                .orderBy(creditsOrderClause, codeOrderClause, (Order) QueryUtils.toOrders(pageable.getSort(), root, builder));

        return entityManager.createQuery(query)
                .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }


}

