package school.hei.haapi.service.sms;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import school.hei.haapi.model.SmsContactGroup;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.SmsContactGroupRepository;
import school.hei.haapi.repository.SmsContactRepository;

@org.springframework.stereotype.Service
@AllArgsConstructor
public class SmsContactGroupService {
  private final SmsContactGroupRepository smsContactGroupRepository;
  private final SmsContactRepository smsContactRepository;

  public List<SmsContactGroup> getByOwner(User owner, Pageable pageable) {
    return smsContactGroupRepository.findAllByOwner_IdAndIsDeletedFalseOrderByCreationDatetimeDesc(
        owner.getId(), pageable);
  }

  public SmsContactGroup getById(String id) {
    return smsContactGroupRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("SMS contact group " + id + " not found"));
  }

  public SmsContactGroup create(User owner, String name, List<String> contactIds) {
    return smsContactGroupRepository.save(
        SmsContactGroup.builder()
            .id(UUID.randomUUID().toString())
            .name(name)
            .owner(owner)
            .members(smsContactRepository.findAllByIdInAndIsDeletedFalse(contactIds))
            .build());
  }

  public SmsContactGroup update(String id, String name, List<String> contactIds) {
    var group = getById(id);
    group.setName(name);
    group.setMembers(smsContactRepository.findAllByIdInAndIsDeletedFalse(contactIds));
    return smsContactGroupRepository.save(group);
  }

  public SmsContactGroup delete(String id) {
    var group = getById(id);
    group.setDeleted(true);
    return smsContactGroupRepository.save(group);
  }

  public SmsContactGroup addMember(String groupId, String contactId) {
    var group = getById(groupId);
    var contact =
        smsContactRepository
            .findById(contactId)
            .orElseThrow(() -> new NotFoundException("SMS contact " + contactId + " not found"));
    if (group.getMembers().stream().noneMatch(m -> m.getId().equals(contactId))) {
      group.getMembers().add(contact);
      smsContactGroupRepository.save(group);
    }
    return group;
  }

  public SmsContactGroup removeMember(String groupId, String contactId) {
    var group = getById(groupId);
    group.getMembers().removeIf(m -> m.getId().equals(contactId));
    smsContactGroupRepository.save(group);
    return group;
  }
}
