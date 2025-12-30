package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
      "select e from Exam e join e.courseAssignment.groups g where g.id = :group_id and "
          + "e.courseAssignment.id = :course_assignment_id")
  Page<Exam> findExamsByGroupIdAndCourseAssignmentId(
      @Param("group_id") String courseId,
      @Param("course_assignment_id") String courseAssignmentId,
      Pageable pageable);

  @Query(
      """
          select distinct u.ref
          from Exam e
          join e.courseAssignment.groups g
          join g.groupFlows gf
          join gf.student u
          where e.id = :exam_id
      """)
  List<String> findStudentRefsByExamId(@Param("exam_id") String examId);
}
