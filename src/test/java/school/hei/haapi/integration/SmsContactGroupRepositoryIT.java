package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.testData.GroupTestData;
import school.hei.haapi.integration.testData.ManagerTestData;
import school.hei.haapi.model.SmsContactGroup;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.GroupRepository;
import school.hei.haapi.repository.SmsContactGroupRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.sms.SmsContactGroupService;

public class SmsContactGroupRepositoryIT extends FacadeITMockedThirdParties {
  @Autowired private UserRepository userRepository;
  @Autowired private GroupRepository groupRepository;
  @Autowired private SmsContactGroupRepository smsContactGroupRepository;
  @Autowired private SmsContactGroupService smsContactGroupService;

  private User aManager() {
    var id = UUID.randomUUID().toString();
    return User.builder()
        .id(id)
        .ref("REF" + id)
        .firstName("Test")
        .lastName("Manager")
        .email("test+" + id + "@hei.school")
        .status(User.Status.ENABLED)
        .role(User.Role.MANAGER)
        .entranceDatetime(Instant.parse("2021-01-01T00:00:00Z"))
        .build();
  }

  @Test
  void findAllVisibleTo_returns_the_callers_own_groups_and_every_student_group_linked_one() {
    var caller = userRepository.save(aManager());
    var otherManager = userRepository.save(ManagerTestData.hasina());
    var ownedByCaller =
        smsContactGroupRepository.save(
            SmsContactGroup.builder()
                .id(UUID.randomUUID().toString())
                .name("Owned by caller")
                .owner(caller)
                .build());
    var ownedByOther =
        smsContactGroupRepository.save(
            SmsContactGroup.builder()
                .id(UUID.randomUUID().toString())
                .name("Owned by someone else")
                .owner(otherManager)
                .build());
    var studentGroup = groupRepository.save(GroupTestData.g1());
    var linkedToStudentGroup =
        smsContactGroupRepository.save(
            SmsContactGroup.builder()
                .id(UUID.randomUUID().toString())
                .name("Contact de " + studentGroup.getName())
                .studentGroup(studentGroup)
                .build());

    var result =
        smsContactGroupRepository.findAllVisibleTo(caller.getId(), "", PageRequest.of(0, 10));

    assertTrue(result.stream().anyMatch(g -> g.getId().equals(ownedByCaller.getId())));
    assertTrue(result.stream().anyMatch(g -> g.getId().equals(linkedToStudentGroup.getId())));
    assertTrue(result.stream().noneMatch(g -> g.getId().equals(ownedByOther.getId())));
  }

  @Test
  void getByOwner_with_no_search_param_does_not_fail_against_a_real_database() {
    var caller = userRepository.save(aManager());
    var ownedByCaller =
        smsContactGroupRepository.save(
            SmsContactGroup.builder()
                .id(UUID.randomUUID().toString())
                .name("Owned by caller, no search")
                .owner(caller)
                .build());

    var result = smsContactGroupService.getByOwner(caller, null, PageRequest.of(0, 10));

    assertTrue(result.stream().anyMatch(g -> g.getId().equals(ownedByCaller.getId())));
  }

  @Test
  void findAllVisibleTo_search_matches_the_group_name_case_insensitively() {
    var caller = userRepository.save(aManager());
    var match =
        smsContactGroupRepository.save(
            SmsContactGroup.builder()
                .id(UUID.randomUUID().toString())
                .name("Promo Rotamahafinaritra")
                .owner(caller)
                .build());
    var noMatch =
        smsContactGroupRepository.save(
            SmsContactGroup.builder()
                .id(UUID.randomUUID().toString())
                .name("Promo Tiana")
                .owner(caller)
                .build());

    var result =
        smsContactGroupRepository.findAllVisibleTo(
            caller.getId(), "rotamahafinaritra", PageRequest.of(0, 10));

    assertTrue(result.stream().anyMatch(g -> g.getId().equals(match.getId())));
    assertTrue(result.stream().noneMatch(g -> g.getId().equals(noMatch.getId())));
  }

  @Test
  void findByStudentGroup_IdAndIsDeletedFalse_finds_the_contact_group_linked_to_a_student_group() {
    var studentGroup = groupRepository.save(GroupTestData.g2());
    var linked =
        smsContactGroupRepository.save(
            SmsContactGroup.builder()
                .id(UUID.randomUUID().toString())
                .name("Contact de " + studentGroup.getName())
                .studentGroup(studentGroup)
                .build());

    var result =
        smsContactGroupRepository.findByStudentGroup_IdAndIsDeletedFalse(studentGroup.getId());

    assertTrue(result.isPresent());
    assertEquals(linked.getId(), result.get().getId());
  }
}
