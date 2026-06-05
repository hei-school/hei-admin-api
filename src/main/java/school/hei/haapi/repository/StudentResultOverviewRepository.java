package school.hei.haapi.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import school.hei.haapi.model.StudentResultOverview;

public interface StudentResultOverviewRepository
    extends JpaRepository<StudentResultOverview, String> {
  @Query(
      "SELECT s FROM StudentResultOverview s JOIN FETCH s.graduationPromotion WHERE s.student.id ="
          + " :studentId")
  Optional<StudentResultOverview> findStudentResultOverviewsByStudentId(
      @Param("studentId") String studentId);

  List<StudentResultOverview> findAllByStudentIdIn(@Param("studentIds") List<String> studentIds);
}
