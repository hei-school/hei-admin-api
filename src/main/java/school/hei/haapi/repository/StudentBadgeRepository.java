package school.hei.haapi.repository;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import school.hei.haapi.model.StudentBadge;

public interface StudentBadgeRepository extends JpaRepository<StudentBadge, String> {
  Optional<StudentBadge> findByPublicId(String publicId);

  boolean existsByPublicId(String publicId);

  Optional<StudentBadge> findByStudentIdAndAcademicYearAndRevocationDatetimeIsNull(
      String studentId, String academicYear);

  boolean existsByStudentIdAndExpirationDatetimeIsNullAndRevocationDatetimeIsNull(String studentId);

  @Query(
      """
      select badge from StudentBadge badge
      where badge.student.id = :studentId
        and badge.revocationDatetime is null
        and (badge.expirationDatetime is null or badge.expirationDatetime > :now)
      order by badge.expirationDatetime desc nulls first
      limit 1
      """)
  Optional<StudentBadge> findCurrentBadgeOfStudent(String studentId, Instant now);
}
