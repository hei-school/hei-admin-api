package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import school.hei.haapi.model.GradeChangeHistory;
import school.hei.haapi.model.dto.GradeDto;

public interface GradeChangeHistoryRepository extends JpaRepository<GradeChangeHistory, String> {
  @Query(
      """
      SELECT gch.id as id, gch.grade.student.ref as ref, gch.score as score
      FROM GradeChangeHistory gch
      WHERE gch.grade.id IN :gradeIds
      ORDER BY gch.changeInstant ASC
      """)
  List<GradeDto> findByGradeIdsOrderedByChangeInstantAsc(@Param("gradeIds") List<String> gradeIds);

  List<String> gradeId(String gradeId);
}
