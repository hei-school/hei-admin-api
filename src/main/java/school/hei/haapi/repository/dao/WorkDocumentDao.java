package school.hei.haapi.repository.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import school.hei.haapi.endpoint.rest.model.ProfessionalExperienceFileTypeEnum;
import school.hei.haapi.model.FeeTemplate;
import school.hei.haapi.model.WorkDocument;

import java.util.List;

@Repository
@AllArgsConstructor
public class WorkDocumentDao {
    private final EntityManager entityManager;

    public List<WorkDocument> findAllByStudentIdAndProfessionalExperienceType(String studentId, ProfessionalExperienceFileTypeEnum professionalExperience, Pageable pageable) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<WorkDocument> query = builder.createQuery(WorkDocument.class);
        Root<WorkDocument> root = query.from(WorkDocument.class);

        
    }
}
