package school.hei.haapi.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.SmsContactGroup;

@Repository
public interface SmsContactGroupRepository extends JpaRepository<SmsContactGroup, String> {
  List<SmsContactGroup> findAllByIdInAndIsDeletedFalse(List<String> ids);

  Optional<SmsContactGroup> findByStudentGroup_IdAndIsDeletedFalse(String groupId);

  @Query(
      "SELECT g FROM SmsContactGroup g WHERE g.isDeleted = false"
          + " AND (g.owner.id = :ownerId OR g.studentGroup IS NOT NULL)"
          + " ORDER BY g.creationDatetime DESC")
  List<SmsContactGroup> findAllVisibleTo(@Param("ownerId") String ownerId, Pageable pageable);
}
