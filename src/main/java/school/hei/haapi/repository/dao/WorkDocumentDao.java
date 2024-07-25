package school.hei.haapi.repository.dao;

import static jakarta.persistence.criteria.JoinType.LEFT;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import school.hei.haapi.endpoint.rest.model.ProfessionalExperienceFileTypeEnum;
import school.hei.haapi.model.User;
import school.hei.haapi.model.WorkDocument;

import java.util.ArrayList;
import java.util.List;

@Repository
@AllArgsConstructor
public class WorkDocumentDao {
    private final EntityManager entityManager;

    public List<WorkDocument> findAllByStudentIdAndProfessionalExperienceType(String studentId, ProfessionalExperienceFileTypeEnum professionalExperience, Pageable pageable) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<WorkDocument> query = builder.createQuery(WorkDocument.class);
        Root<WorkDocument> root = query.from(WorkDocument.class);
        Join<WorkDocument, User> studentJoin = root.join("student", LEFT);
        List<Predicate> predicates = new ArrayList<>();

        Expression<String> studentIdExpression = studentJoin.get("id");
        predicates.add(builder.equal(studentIdExpression, studentId));

        if (professionalExperience != null) {
            Expression<ProfessionalExperienceFileTypeEnum> professionalExpExpression = root.get("professionalExperienceType");
            predicates.add(builder.and(builder.equal(professionalExpExpression, professionalExperience)));
        }

        query.distinct(true).where(predicates.toArray(new Predicate[0]));

        return entityManager
                .createQuery(query)
                .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }
}
