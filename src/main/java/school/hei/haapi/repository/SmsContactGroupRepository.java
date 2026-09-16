package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.SmsContactGroup;

@Repository
public interface SmsContactGroupRepository extends JpaRepository<SmsContactGroup, String> {
  List<SmsContactGroup> findAllByOwner_IdAndIsDeletedFalseOrderByCreationDatetimeDesc(
      String ownerId, Pageable pageable);

  List<SmsContactGroup> findAllByIdInAndIsDeletedFalse(List<String> ids);
}
