package school.hei.haapi.repository;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.haapi.model.StudentBadge;

public interface StudentBadgeRepository extends JpaRepository<StudentBadge, String> {
  Optional<StudentBadge> findByPublicId(String publicId);

  Optional<StudentBadge> findByStudentIdAndAcademicYearAndRevocationDatetimeIsNull(
      String studentId, String academicYear);

  Optional<StudentBadge>
      findFirstByStudentIdAndRevocationDatetimeIsNullAndExpirationDatetimeAfterOrderByExpirationDatetimeDesc(
          String studentId, Instant now);

  boolean existsByPublicId(String publicId);
}
