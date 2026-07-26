package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.model.GroupFlow;

@Repository
public interface GroupFlowRepository extends JpaRepository<GroupFlow, String> {
  @Query(
      """
      SELECT gf FROM GroupFlow gf
      JOIN gf.group g
      JOIN CourseAssignment ca ON g MEMBER OF ca.groups
      JOIN ca.course c
      WHERE gf.student.id = :student_id
      AND c.studentLevel = :level
      """)
  List<GroupFlow> findByFlowTypeAndStudentAndLevel(
      @Param("student_id") String studentId, @Param("level") StudentLevel level);
}
