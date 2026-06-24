package school.hei.haapi.repository;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
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
      select distinct c.studentLevel
                from Exam e
                join e.courseAssignment ca
                join ca.course c
                join ca.groups g
                where g.id = :group_id
                  and e.examinationDate <= :examination_date
                order by c.studentLevel
      """)
  List<StudentLevel> findStudentLevelsByGroupBeforeExaminationDate(
      @Param("group_id") String groupId, @Param("examination_date") Instant examinationDate);
}
