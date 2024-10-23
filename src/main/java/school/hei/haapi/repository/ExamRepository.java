package school.hei.haapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Exam;

@Repository
public interface ExamRepository extends JpaRepository<Exam, String> {
  @Query(
      "select e from Exam e where e.awardedCourse.group.id = :group_id "
          + "and e.awardedCourse.id = :awarded_course_id and e.id = :exam_id")
  Exam findExamsByIdAndGroupIdAndAwardedGroupId(
      @Param("exam_id") String examId,
      @Param("awarded_course_id") String awardedCourseId,
      @Param("group_id") String groupId);

  @Query("select e from Exam e where e.awardedCourse.course.id = :course_id ")
  List<Exam> findExamsByCourseId(@Param("course_id") String courseId);

  @Query("select e from Exam e where e.id = :exam_id and e.awardedCourse.id = :awardedCourseId")
  Optional<Exam> findByIdAndAwardedCourseId(@Param("exam_id") String exam_id,
                                            @Param("awardedCourseId") String awardedCourseId);

  @Query(
      "select e from Exam e where e.awardedCourse.id = :awarded_course_id")
  Page<Exam> findExamsByAwardedGroupId(
      @Param("awarded_course_id") String awardedCourseId,
      Pageable pageable);
}
