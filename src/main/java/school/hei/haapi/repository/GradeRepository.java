package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.User;
import school.hei.haapi.model.dto.GradeDto;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, String> {
  @Query("select g from Grade g where  g.exam.id = :exam_id and g.student.id = :student_id")
  Optional<Grade> getGradeByExamIdAndStudentId(
      @Param("exam_id") String examId, @Param("student_id") String studentId);

  @Query("select g from Grade g where  g.exam.id = :exam_id and g.student.ref = :student_ref")
  Optional<Grade> getGradeByExamIdAndStudentRef(
      @Param("exam_id") String examId, @Param("student_ref") String studentRef);

  Optional<Grade> findByExamIdAndStudentId(String examId, String studentId);

  @Query(
      "select g from Grade g where g.student.id = :student_id and g.exam.courseAssignment.course.id"
          + " = :course_id")
  List<Grade> getGradesByStudentIdAndCourseId(
      @Param("student_id") String studentId, @Param("course_id") String courseId);

  List<Grade> getAllByStudent(User student);

  @Query(
      "select new school.hei.haapi.model.dto.GradeDto(g.id, g.student.ref, g.score) from Grade g"
          + " where g.exam.id = :exam_id and g.student.status in :statuses")
  List<GradeDto> getGradesByExamId(
      @Param("exam_id") String examId, @Param("statuses") List<User.Status> statuses);

  @Query(
      """
      select g
      from Grade g
      left join fetch g.gradeChangeHistories
      where g.exam.courseAssignment.id in :courseAssignmentIds
        and g.student.id = :studentId
      """)
  List<Grade> findGradesByCourseAssignmentIdsAndStudentId(
      @Param("courseAssignmentIds") List<String> courseAssignmentIds, @Param("studentId") String studentId);
}
