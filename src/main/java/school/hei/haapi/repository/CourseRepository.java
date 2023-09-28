package school.hei.haapi.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.Exam;

@Repository
public interface CourseRepository extends JpaRepository<Course, String> {
  boolean existsByCode(String code);
  Course getCourseById(String id);

//  @Query("select c from Course c FULL JOIN  where c.code ilike :code and c.name ilike :name and c.credits ilike :credits and c.awardedCourse")
//  Exam findByCriteria(@Param("exam_id") String examId, @Param("awarded_course_id") String awardedCourseId, @Param("group_id") String groupId);
//
//  String code, String name, Integer credits,
//  String teacherFirstName, String teacherLastName,
//  String creditsOrder, String codeOrder,
//  Pageable pageable
}
