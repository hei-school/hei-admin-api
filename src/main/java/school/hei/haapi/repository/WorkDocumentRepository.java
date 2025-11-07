package school.hei.haapi.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.WorkDocument;
import school.hei.haapi.model.dto.StatisticsStudentAlternatingDto;

@Repository
public interface WorkDocumentRepository extends JpaRepository<WorkDocument, String> {
  Optional<WorkDocument> findTopByStudentIdOrderByCreationDatetimeDesc(String studentId);

  @Query(
      value =
          """
select
  count(*) as total,
  coalesce(sum(case when latest_wd2.commitment_end > now() then 1 else 0 end), 0) as haveBeenWorking,
  coalesce(sum(case when latest_wd2.commitment_begin > now() then 1 else 0 end), 0) as willWork,
  coalesce(sum(case when latest_wd2 is null then 1 else 0 end), 0) as notWorking,
  coalesce(sum(
      case when latest_wd2.commitment_begin <= now()
      and latest_wd2.commitment_end >= now() then 1 else 0 end), 0) as working
from "user" left join (
    select wd.*
    from work_document wd inner join (
      select student_id, max(creation_datetime) as max_creation_datetime
      from work_document latest_wd group by student_id
    ) as latest_wd on wd.student_id = latest_wd.student_id and wd.creation_datetime = latest_wd.max_creation_datetime
) as latest_wd2 on "user".id = latest_wd2.student_id
where role = 'STUDENT' and "user".status <> 'DISABLED'
""",
      nativeQuery = true)
  StatisticsStudentAlternatingDto getStudentAlternatingStatistics();
}
