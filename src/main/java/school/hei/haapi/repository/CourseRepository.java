package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.StudentCourse;

@Repository
public interface CourseRepository extends JpaRepository<Course, String> {
  Course findCourseById(String courseId);

  boolean existsByCode(String code);

  @Query(value = "select c from Course c WHERE " +
      "lower(c.code) like lower(concat('%', :code,'%'))" +
      "AND lower(c.name) like lower(concat('%', :name,'%'))" +
      "AND (:credits is null or c.credits = :credits) " +
      "AND lower(c.mainTeacher.firstName) like lower(concat('%', :teacherFirstName,'%')) " +
      "AND lower(c.mainTeacher.lastName) like lower(concat('%', :teacherLastName,'%'))")
  List<Course> findAllFiltered(@Param("code") String code,
                               @Param("name") String name,
                               @Param("credits") Integer credits,
                               @Param("teacherFirstName") String teacherFirstName,
                               @Param("teacherLastName") String teacherLastName,
                               Pageable pageable);

  @Query("select c from Course c "
      + "LEFT JOIN StudentCourse sc ON c.id = sc.course.id "
      + "AND sc.student.id = :studentId "
      + "WHERE sc.student.id != :studentId or sc.student.id is null"
      + " OR sc.status = :status")
  List<Course> findCoursesByStudentIdAndStatusOrUnlinked(@Param("studentId") String studentId,
                                                         @Param("status")
                                                         StudentCourse.CourseStatus status);

  @Query("select c from Course c "
      + "LEFT JOIN StudentCourse sc ON c.id = sc.course.id "
      + "AND sc.student.id = :studentId "
      + "WHERE sc.student.id = :studentId OR sc.status = :status")
  List<Course> findCoursesByStudentIdAndStatusLinked(@Param("studentId") String studentId,
                                                     @Param("status")
                                                     StudentCourse.CourseStatus status);

}

