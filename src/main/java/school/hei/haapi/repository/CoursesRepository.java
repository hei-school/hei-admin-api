package school.hei.haapi.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.haapi.model.Courses;
import school.hei.haapi.model.User;

import java.util.List;

public interface CoursesRepository extends JpaRepository<Courses, String> {
    List<Courses> findByCodeAndRefContainingIgnoreCaseAndNameContainingIgnoreCaseAndCreditsContainingIgnoreCaseAndTeacher_first_NameContainingIgnoreCaseAndTeacher_last_nameContainingIgnoreCase(
             String code, String name, Integer credits, String teacher_first_name, String teacher_last_name, Pageable pageable);
}


