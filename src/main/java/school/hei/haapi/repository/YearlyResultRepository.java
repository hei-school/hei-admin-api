package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.model.YearlyResult;

public interface YearlyResultRepository extends JpaRepository<YearlyResult, String> {
  List<YearlyResult> findAllByStudent_IdIn(@Param("studentIds") List<String> studentIds);

  @Query(
      "SELECT yr FROM YearlyResult yr WHERE yr.group.id IN :groupIds AND yr.level = :level"
          + " ORDER BY yr.weightedAverage DESC NULLS LAST")
  List<YearlyResult> findAllByGroupIdsAndLevelOrderByWeightedAverageDesc(
      @Param("groupIds") List<String> groupIds, @Param("level") StudentLevel level);
}
