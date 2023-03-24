package school.hei.haapi.repository.dao;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
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
import java.util.Objects;

@Repository
@AllArgsConstructor
public class CourseManagerDao {
    private EntityManager entityManager;

    public List<Course> findByCriteria(String code,
                                       String name,
                                       Integer credits,
                                       String firstName,
                                       String lastName,
                                       String creditsOrder,
                                       String codeOrder,
                                       Pageable pageable) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Course> query = builder.createQuery(Course.class);
        Root<Course> root = query.from(Course.class);
        Join<Course, User> teacher = root.join("mainTeacher");

        Predicate hasCourseCode =
                builder.or(
                        builder.like(builder.lower(root.get("code")), "%" + code.toLowerCase() + "%"),
                        builder.like(root.get("code"), "%" + code + "%")
                );

        Predicate hasCourseName =
                builder.or(
                        builder.like(builder.lower(root.get("name")), "%" + name.toLowerCase() + "%"),
                        builder.like(root.get("name"), "%" + name + "%")
                );

        Predicate hasTeacherFirstName =
                builder.or(
                        builder.like(builder.lower(teacher.get("firstName")), "%" + firstName.toLowerCase() + "%"),
                        builder.like(teacher.get("firstName"), "%" + firstName + "%")
                );
        Predicate hasTeacherLastName =
                builder.or(
                        builder.like(builder.lower(teacher.get("lastName")), "%" + lastName.toLowerCase() + "%"),
                        builder.like(teacher.get("lastName"), "%" + lastName + "%")
                );

        Predicate hasCourseCredits = builder.equal(root.get("credits"), credits);

        Predicate hasTeacherFirstNameAndHasTeacherLastName = null;
        hasTeacherFirstNameAndHasTeacherLastName = (firstName.length()>0 && lastName.length()>0)?
                builder.or(hasTeacherFirstName,hasTeacherLastName):
                builder.and(hasTeacherFirstName,hasTeacherLastName);
        
        if (credits==null){
            query
                    .where(builder.and(hasCourseCode,hasCourseName,hasTeacherFirstNameAndHasTeacherLastName));
        }else {
            query
                    .where(builder.and(hasCourseCode,hasCourseName,hasTeacherFirstNameAndHasTeacherLastName,hasCourseCredits));
        }

        javax.persistence.criteria.Order codeArrange = builder.desc(root.get("code"));
        if (Objects.equals(codeOrder, "ASC")){
            codeArrange = builder.asc(root.get("code"));
        }
        javax.persistence.criteria.Order creditsArrange = builder.desc(root.get("credits"));
        if (Objects.equals(creditsOrder, "ASC")){
            creditsArrange = builder.asc(root.get("credits"));
        }

        if (creditsOrder.length()>0 && codeOrder.length()>0){
            query.orderBy(creditsArrange,codeArrange);
        } else if (creditsOrder.length()>0) {
            query.orderBy(creditsArrange);
        } else if (codeOrder.length()>0) {
            query.orderBy(codeArrange);
        }

        return entityManager.createQuery(query)
                .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }
}