package school.hei.haapi.service.sms;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.endpoint.rest.mapper.SmsContactMapper;
import school.hei.haapi.model.SmsContact;
import school.hei.haapi.model.SmsContactOwnerRole;
import school.hei.haapi.model.User;
import school.hei.haapi.model.dto.SmsFileImportRowDto;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.SmsContactRepository;
import school.hei.haapi.repository.dao.SmsContactDao;

@Slf4j
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

  @Transactional
  public Optional<SmsContact> createContactIfMissing(User user) {
    if (user.getStatus() != User.Status.ENABLED) {
      return Optional.empty();
    }
    if (user.getPhone() == null || user.getPhone().isBlank()) {
      return Optional.empty();
    }
    var normalizedPhone = SmsFileImportRowDto.normalizePhoneNumber(user.getPhone());
    if (normalizedPhone.length() < 6) {
      return Optional.empty();
    }
    if (smsContactRepository.existsByOwner_Id(user.getId())) {
      return Optional.empty();
    }
    var contact =
        SmsContact.builder()
            .id(UUID.randomUUID().toString())
            .phoneNumber(normalizedPhone)
            .name(SmsContactMapper.toContactName(user))
            .owner(user)
            .ownerRole(SmsContactOwnerRole.valueOf(user.getRole().name()))
            .build();
    try {
      return Optional.of(smsContactRepository.save(contact));
    } catch (DataIntegrityViolationException e) {
      log.info("SMS contact already exists for user {}, skipping", user.getId());
      return Optional.empty();
    }
  }
}
