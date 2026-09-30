package school.hei.haapi.service.sms;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.SmsContactGroup;
import school.hei.haapi.repository.SmsContactGroupRepository;
import school.hei.haapi.repository.SmsContactRepository;

@Service
@AllArgsConstructor
public class StudentGroupContactSyncService {
  private static final String CONTACT_GROUP_NAME_PREFIX = "Contact de ";

  private final SmsContactGroupRepository smsContactGroupRepository;
  private final SmsContactRepository smsContactRepository;

  public SmsContactGroup createContactGroupFor(Group group) {
    return smsContactGroupRepository.save(
        SmsContactGroup.builder()
            .id(UUID.randomUUID().toString())
            .name(CONTACT_GROUP_NAME_PREFIX + group.getName())
            .studentGroup(group)
            .build());
  }

  public void syncMembership(List<GroupFlow> groupFlows) {
    groupFlows.forEach(this::syncMembership);
  }

  public void syncMembership(GroupFlow groupFlow) {
    var contactGroup =
        smsContactGroupRepository.findByStudentGroup_IdAndIsDeletedFalse(
            groupFlow.getGroup().getId());
    if (contactGroup.isEmpty()) {
      return;
    }
    var studentContact =
        smsContactRepository.findByOwner_IdAndIsDeletedFalse(groupFlow.getStudent().getId());
    if (studentContact.isEmpty()) {
      return;
    }

    var members = contactGroup.get().getMembers();
    var alreadyMember =
        members.stream().anyMatch(member -> member.getId().equals(studentContact.get().getId()));

    if (groupFlow.getGroupFlowType() == GroupFlow.GroupFlowType.JOIN) {
      if (!alreadyMember) {
        members.add(studentContact.get());
        smsContactGroupRepository.save(contactGroup.get());
      }
    } else if (alreadyMember) {
      members.removeIf(member -> member.getId().equals(studentContact.get().getId()));
      smsContactGroupRepository.save(contactGroup.get());
    }
  }
}
