package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.StudentCourses;
import school.hei.haapi.model.User;

import java.util.List;

@Repository
public interface StudentCoursesRepository extends JpaRepository<StudentCourses, Integer> {
    List<StudentCourses> findByStudent(User student);
}
