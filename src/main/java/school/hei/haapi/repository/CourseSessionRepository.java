package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.CourseSession;
import school.hei.haapi.model.StudentAttendance;

@Repository
public interface CourseSessionRepository extends JpaRepository<CourseSession, String> {
}
