package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.StudentCourse;

@Repository
public interface CourseRepository extends JpaRepository<Course, String> {
  Course findCourseById(String courseId);

  Course findCourseByCode(String code);

  @Query("select c from Course c " +
      "LEFT JOIN StudentCourse sc ON c.id = sc.course.id " +
      "AND sc.student.id = :studentId " +
      "WHERE sc.student.id IS NULL OR sc.status = :status")
  List<Course> findCoursesByStudentIdAndStatusOrUnlinked(@Param("studentId") String studentId,
                                                         @Param("status")
                                                         StudentCourse.CourseStatus status);

  @Query("select c from Course c " +
      "LEFT JOIN StudentCourse sc ON c.id = sc.course.id " +
      "AND sc.student.id = :studentId " +
      "WHERE sc.student.id = :studentId OR sc.status = :status")
  List<Course> findCoursesByStudentIdAndStatusLinked(@Param("studentId") String studentId,
                                                     @Param("status")
                                                     StudentCourse.CourseStatus status);

}

