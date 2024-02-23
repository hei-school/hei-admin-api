package school.hei.haapi.repository.dao;

import static jakarta.persistence.criteria.JoinType.LEFT;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import school.hei.haapi.endpoint.rest.model.FileType;
import school.hei.haapi.model.FileInfo;
import school.hei.haapi.model.User;

@Repository
@AllArgsConstructor
public class FileInfoDao {
  private final EntityManager entityManager;

  public List<FileInfo> findAllByCriteria(String userId, FileType fileType) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<FileInfo> query = builder.createQuery(FileInfo.class);
    Root<FileInfo> root = query.from(FileInfo.class);
    Join<FileInfo, User> userJoin = root.join("user", LEFT);
    List<Predicate> predicates = new ArrayList<>();

    if (fileType != null) {
      Expression<FileType> fileTypeExpression = root.get("fileType");
      predicates.add(builder.and(builder.equal(fileTypeExpression, fileType)));
    }

    if (userId != null) {
      Expression<String> userIdExpression = userJoin.get("id");
      predicates.add(builder.and(builder.equal(userIdExpression, userId)));
    }

    query.where(predicates.toArray(new Predicate[0]));

    return entityManager.createQuery(query).getResultList();
  }
}
