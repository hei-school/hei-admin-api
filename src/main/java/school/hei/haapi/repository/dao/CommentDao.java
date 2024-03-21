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
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Comment;
import school.hei.haapi.model.User;

@Repository
@AllArgsConstructor
public class CommentDao {
  private final EntityManager entityManager;

  public List<Comment> filterCommentsByCriteria(String studentRef, Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Comment> query = builder.createQuery(Comment.class);
    Root<Comment> root = query.from(Comment.class);
    Join<Comment, User> userJoin = root.join("subject", LEFT);
    List<Predicate> predicates = new ArrayList<>();

    if (studentRef != null && !studentRef.isEmpty()) {
      Expression studentRefExpression = userJoin.get("ref");
      predicates.add(builder.and(builder.equal(studentRefExpression, studentRef)));
    }

    query
        .distinct(true)
        .orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder))
        .where(predicates.toArray(new Predicate[0]));

    return entityManager
        .createQuery(query)
        .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }
}
