package school.hei.haapi.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Course;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, String> {

//    @Query(
//            value = "select c from Course c where " +
//                    "(:code IS NULL OR LOWER(c.code) LIKE CONCAT('%', LOWER(:code) , '%')) OR " +
//                    "(:name IS NULL OR LOWER(c.name) LIKE CONCAT('%', LOWER(:name), '%')) OR " +
//                    "(:teacher_first_name IS NULL OR LOWER(c.mainTeacher.firstName) LIKE CONCAT('%', LOWER(:teacher_first_name), '%')) OR " +
//                    "(:teacher_last_name IS NULL OR LOWER(c.mainTeacher.lastName) LIKE CONCAT('%', LOWER(:teacher_last_name), '%')) OR " +
//                    "(:credits IS NULL OR c.credits = :credits) " +
//                    "ORDER BY " +
//                    "CASE WHEN :credits_order = 'ASC' THEN c.credits END ASC, " +
//                    "CASE WHEN :credits_order = 'DESC' THEN c.credits END DESC, " +
//                    "CASE WHEN :code_order = 'ASC' THEN c.code END ASC, " +
//                    "CASE WHEN :code_order = 'DESC' THEN c.code END DESC"
//    )
//    List<Course> findAllByCriteria(
//            @Param("code") String code,
//            @Param("name") String name,
//            @Param("credits") Integer credits,
//            @Param("teacher_first_name") String teacherFirstName,
//            @Param("teacher_last_name") String teacherLastName,
//            @Param("credits_order") String creditsOrder,
//            @Param("code_order") String codeOrder,
//            Pageable pageable
//    );
}
