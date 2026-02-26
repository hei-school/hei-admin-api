package school.hei.haapi.repository;

import java.util.LinkedHashSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Promotion;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, String> {
  @Query(
      """
          SELECT DISTINCT p
          FROM Promotion p
          JOIN p.groups g
          JOIN g.groupFlows gf
          WHERE gf.student.id = :studentId
          ORDER BY p.startDatetime ASC
      """)
  LinkedHashSet<Promotion> findAllByStudentIdOrderByStartDateTime(String studentId);
}
