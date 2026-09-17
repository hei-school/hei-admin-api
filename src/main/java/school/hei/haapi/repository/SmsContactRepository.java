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
}
