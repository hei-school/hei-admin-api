package school.hei.haapi.service.sms;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import school.hei.haapi.endpoint.rest.mapper.SmsContactMapper;
import school.hei.haapi.model.SmsContact;
import school.hei.haapi.model.SmsContactOwnerRole;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.SmsContactRepository;
import school.hei.haapi.repository.dao.SmsContactDao;

@org.springframework.stereotype.Service
@AllArgsConstructor
public class SmsContactService {
  private final SmsContactRepository smsContactRepository;
  private final SmsContactDao smsContactDao;
  private final SmsContactMapper smsContactMapper;

  private SmsContact create(User user) {
    return smsContactRepository.save(
        SmsContact.builder()
            .id(UUID.randomUUID().toString())
            .phoneNumber(user.getPhone())
            .name(SmsContactMapper.toContactName(user))
            .owner(user)
            .ownerRole(toOwnerRole(user.getRole()))
            .build());
  }

  private SmsContactOwnerRole toOwnerRole(User.Role role) {
    return switch (role) {
      case ADMIN -> SmsContactOwnerRole.ADMIN;
      case MONITOR -> SmsContactOwnerRole.MONITOR;
      case STUDENT -> SmsContactOwnerRole.STUDENT;
      case MANAGER -> SmsContactOwnerRole.MANAGER;
      default -> SmsContactOwnerRole.MANAGER;
    };
  }

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
