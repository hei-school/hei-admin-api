package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.CourseSession;

@Repository
public interface CourseSessionRepository extends JpaRepository<CourseSession, String> {
}
