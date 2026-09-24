package school.hei.haapi.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.SmsContact;

@Repository
public interface SmsContactRepository extends JpaRepository<SmsContact, String> {
  Optional<SmsContact> findByOwner_IdAndIsDeletedFalse(String ownerId);

  List<SmsContact> findAllByIdInAndIsDeletedFalse(List<String> ids);

  // Deliberately ignores is_deleted: sms_contact.owner_id is unique regardless of deletion
  // status, so this is the check that actually predicts whether an insert would violate that
  // constraint (findByOwner_IdAndIsDeletedFalse alone would miss a soft-deleted row).
  boolean existsByOwner_Id(String ownerId);
}
