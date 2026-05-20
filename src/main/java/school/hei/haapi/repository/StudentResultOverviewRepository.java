package school.hei.haapi.repository;

import java.awt.print.Pageable;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import school.hei.haapi.model.ResultOverviewStatus;
import school.hei.haapi.model.StudentResultOverview;

public interface StudentResultOverviewRepository
    extends JpaRepository<StudentResultOverview, String> {

  @Query(
      """
    SELECT sro
    FROM StudentResultOverview sro
    WHERE sro.promotion.id = :promotionId
    and sro.status = :status
""")
  List<StudentResultOverview> findAllByStudentIds(
      String promotionId, ResultOverviewStatus status, Pageable pageable);
}
