package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.testData.ManagerTestData;
import school.hei.haapi.model.SmsContact;
import school.hei.haapi.model.SmsContactGroup;
import school.hei.haapi.model.SmsContactOwnerRole;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.SmsContactGroupRepository;
import school.hei.haapi.repository.SmsContactRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.dao.SmsContactDao;

/**
 * Real-DB check for filterByCriteria(contactGroupId, ...) — the unit test for this DAO mocks
 * EntityManager, so it never actually verifies the isMember(...) Criteria query is correct against
 * real join tables.
 */
public class SmsContactDaoIT extends FacadeITMockedThirdParties {
  @Autowired private UserRepository userRepository;
  @Autowired private SmsContactRepository smsContactRepository;
  @Autowired private SmsContactGroupRepository smsContactGroupRepository;
  @Autowired private SmsContactDao smsContactDao;

  @Test
  void filterByCriteria_by_group_id_returns_only_that_groups_members() {
    var groupOwner = userRepository.save(ManagerTestData.hasina());
    var inGroup1 = smsContactRepository.save(aContact(userRepository.save(aUser()), "0321111111"));
    var inGroup2 = smsContactRepository.save(aContact(userRepository.save(aUser()), "0321111112"));
    var notInGroup =
        smsContactRepository.save(aContact(userRepository.save(aUser()), "0321111113"));

    var group =
        smsContactGroupRepository.save(
            SmsContactGroup.builder()
                .id(UUID.randomUUID().toString())
                .name("Promo 2026")
                .owner(groupOwner)
                .members(List.of(inGroup1, inGroup2))
                .build());

    var result = smsContactDao.filterByCriteria(group.getId(), null, PageRequest.of(0, 10));

    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(c -> c.getId().equals(inGroup1.getId())));
    assertTrue(result.stream().anyMatch(c -> c.getId().equals(inGroup2.getId())));
    assertTrue(result.stream().noneMatch(c -> c.getId().equals(notInGroup.getId())));
  }

  @Test
  void filterByCriteria_combines_group_id_and_owner_role() {
    var groupOwner = userRepository.save(ManagerTestData.hasina());
    var student =
        smsContactRepository.save(
            aContact(userRepository.save(aUser()), "0321111121", SmsContactOwnerRole.STUDENT));
    var manager =
        smsContactRepository.save(
            aContact(userRepository.save(aUser()), "0321111122", SmsContactOwnerRole.MANAGER));

    var group =
        smsContactGroupRepository.save(
            SmsContactGroup.builder()
                .id(UUID.randomUUID().toString())
                .name("Mixed group")
                .owner(groupOwner)
                .members(List.of(student, manager))
                .build());

    var result =
        smsContactDao.filterByCriteria(
            group.getId(), SmsContactOwnerRole.STUDENT, PageRequest.of(0, 10));

    assertEquals(1, result.size());
    assertEquals(student.getId(), result.get(0).getId());
  }

  // Each SmsContact needs its own distinct owner: sms_contact.owner_id is unique.
  private User aUser() {
    var id = UUID.randomUUID().toString();
    return User.builder()
        .id(id)
        .ref("REF" + id)
        .firstName("Test")
        .lastName("User")
        .email("test+" + id + "@hei.school")
        .status(User.Status.ENABLED)
        .role(User.Role.STUDENT)
        .entranceDatetime(java.time.Instant.parse("2021-01-01T00:00:00Z"))
        .build();
  }

  private SmsContact aContact(User owner, String phoneNumber) {
    return aContact(owner, phoneNumber, SmsContactOwnerRole.MANAGER);
  }

  private SmsContact aContact(User owner, String phoneNumber, SmsContactOwnerRole ownerRole) {
    return SmsContact.builder()
        .id(UUID.randomUUID().toString())
        .phoneNumber(phoneNumber)
        .name("Test Contact")
        .owner(owner)
        .ownerRole(ownerRole)
        .build();
  }
}
