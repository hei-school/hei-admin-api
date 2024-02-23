package school.hei.haapi.repository.dao;

import static javax.persistence.criteria.JoinType.LEFT;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
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
    CriteriaQuery query = builder.createQuery(FileInfo.class);
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
