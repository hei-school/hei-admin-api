package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Course;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, String> {
    /* List<Course> findAllByNameIgnoreCaseAndOrMainTeacherFirstNameIgnoreCaseAndOrMainTeacherLastNameIgnoreCaseAndCodeAndCredits();*/
    List<Course> findAllByNameIgnoreCaseAndOrMainTeacherFirstNameAndOrMainTeacherLastNameAndOrCodeAnAndOrCreditsOrderByCodeDescCreditsDesc(
            String name, String firstName, String lastName, String code, int credits);
}
