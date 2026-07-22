package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Exam;

@Repository
public interface ExamRepository extends JpaRepository<Exam, String> {
  @Query(
      """
      select distinct e
      from Exam e
      join e.courseAssignment.groups g
      where e.courseAssignment.course.id = :courseId
        and g.id in :groupIds
      """)
  List<Exam> findExamsByCourseIdAndGroupId(
      @Param("courseId") String courseId, @Param("groupIds") List<String> groupIds);
}
