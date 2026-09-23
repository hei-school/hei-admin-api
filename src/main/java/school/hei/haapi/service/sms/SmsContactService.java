package school.hei.haapi.service.sms;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import school.hei.haapi.model.SmsContact;
import school.hei.haapi.model.SmsContactOwnerRole;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.SmsContactRepository;
import school.hei.haapi.repository.dao.SmsContactDao;

@org.springframework.stereotype.Service
@AllArgsConstructor
public class SmsContactService {
  private final SmsContactRepository smsContactRepository;
  private final SmsContactDao smsContactDao;

  public List<SmsContact> getByCriteria(
      String contactGroupId, SmsContactOwnerRole ownerRole, Pageable pageable) {
    return smsContactDao.filterByCriteria(contactGroupId, ownerRole, pageable);
  }

  public SmsContact getById(String id) {
    return smsContactRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("SMS contact " + id + " not found"));
  }

  public SmsContact delete(String id) {
    var contact = getById(id);
    contact.setDeleted(true);
    return smsContactRepository.save(contact);
  }
}
