package school.hei.haapi.repository.dao;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Course;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import school.hei.haapi.model.User;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
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

        List<Order> codeOrderClause = new ArrayList<>();
        if (creditsOrder == Course.CreditsOrder.ASC) {
            codeOrderClause.add(builder.asc(root.get("code")));
        } else if (creditsOrder == Course.CreditsOrder.DESC) {
            codeOrderClause.add(builder.desc(root.get("code")));
        }
        codeOrderClause.addAll(QueryUtils.toOrders(pageable.getSort(), root, builder));

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


        List<Order> creditsOrderClause = new ArrayList<>();
        if (creditsOrder == Course.CreditsOrder.ASC) {
            creditsOrderClause.add(builder.asc(root.get("credits")));
        } else if (creditsOrder == Course.CreditsOrder.DESC) {
            creditsOrderClause.add(builder.desc(root.get("credits")));
        }
        creditsOrderClause.addAll(QueryUtils.toOrders(pageable.getSort(), root, builder));

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
                .where(builder.and(hasCourseCode, hasCourseName, hasCredits, hasTeacherFirstNameAndTeacherLastName))
                .orderBy(creditsOrderClause);

        return entityManager.createQuery(query)
                .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }


}

