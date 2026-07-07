package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Exam;

@Repository
public interface ExamRepository extends JpaRepository<Exam, String> {
  @Query("select e from Exam e where e.courseAssignment.course.id = :course_id ")
  List<Exam> findExamsByCourseId(@Param("course_id") String courseId);

  @Query("select e from Exam e where e.courseAssignment.id in :course_assignment_ids ")
  List<Exam> findExamsByCourseAssignmentIdIn(
      @Param("course_assignment_ids") List<String> courseAssignmentIds);

  @Query(
      "select e from Exam e join e.courseAssignment.groups g where g.id = :group_ids and "
          + "e.courseAssignment.course.id = : course_id")
  List<Exam> findExamsByCourseIdAndGroupIds(
      @Param("group_ids") String groupId, @Param("course_id") String courseId);
}
