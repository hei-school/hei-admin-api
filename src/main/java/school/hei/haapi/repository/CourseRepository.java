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
    @Query(value = "select c from Course c join c.mainTeacher u"
            + " where (:first_name is null or u.firstName = :first_name) and "
            + "(:last_name is null or u.lastName = :last_name)")
    List<Course> getByTeacherFirstAndLastName(
            @Param("first_name") String firstName, @Param("last_name") String lastName, Pageable pageable);

    @Query(value = "select c from Course c"
            + " where c.code = :code")
    List<Course> getByCode(
            @Param("code") String code, Pageable pageable);

    @Query(value = "select c from Course c"
            + " where c.credits = :credits")
    List<Course> getByCredits(
            @Param("credits") Integer credits, Pageable pageable);

    @Query(value = "select c from Course c"
            + " where c.name = :name")
    List<Course> getByName(
            @Param("name") String name, Pageable pageable);
}
