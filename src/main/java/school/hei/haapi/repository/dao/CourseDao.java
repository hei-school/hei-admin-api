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

    public List<Course> findByCriteria(String code, String name, Integer credits, String teacherFirstName,
                                       String teacherLastName, Course.OrderType creditsOrder, Course.OrderType codeOrder,
                                         Pageable pageable) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Course> query = builder.createQuery(Course.class);
        Root<Course> root = query.from(Course.class);
        Join<Course, User> teacherJoin = root.join("mainTeacher");


        Predicate hasCourseCode =
                builder.or(
                        builder.like(builder.lower(root.get("code")), "%" + code.toLowerCase() + "%"),
                        builder.like(root.get("code"), "%" + code.toLowerCase() + "%")
                );

        Predicate hasCourseName =
                builder.or(
                        builder.like(builder.lower(root.get("name")), "%" + name.toLowerCase() + "%"),
                        builder.like(root.get("name"), "%" + name.toLowerCase() + "%")
                );

        Predicate hasCredits =
                credits != null ?
                        builder.or(
                                builder.equal(root.get("credits"), credits)
                        ) : builder.conjunction();

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


        List<Order> orders = new ArrayList<>();

        if (creditsOrder == Course.OrderType.ASC) {
            orders.add(builder.asc(root.get("credits")));
        } else if (creditsOrder == Course.OrderType.DESC) {
            orders.add(builder.desc(root.get("credits")));
        }

        if (codeOrder == Course.OrderType.ASC) {
            orders.add(builder.asc(root.get("code")));
        } else if (codeOrder == Course.OrderType.DESC) {
            orders.add(builder.desc(root.get("code")));
        }

        if (orders.isEmpty()) {
            orders.add(builder.asc(root.get("code")));
        }

        query
                .where(builder.and(hasCourseCode, hasCourseName, hasCredits, hasTeacherFirstNameAndTeacherLastName))
                .orderBy(orders)
                .orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder));

        return entityManager.createQuery(query)
                .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }


}

