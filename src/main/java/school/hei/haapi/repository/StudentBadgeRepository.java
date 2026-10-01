package school.hei.haapi.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.haapi.model.StudentBadge;

public interface StudentBadgeRepository extends JpaRepository<StudentBadge, String> {
  Optional<StudentBadge> findByPublicId(String publicId);

  Optional<StudentBadge> findByStudentIdAndRevocationDatetimeIsNull(String studentId);

  boolean existsByPublicId(String publicId);
}
