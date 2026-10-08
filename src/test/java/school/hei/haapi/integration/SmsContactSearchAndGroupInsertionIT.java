package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.SmsContact;
import school.hei.haapi.model.SmsContactOwnerRole;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.SmsContactRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.sms.SmsContactGroupService;
import school.hei.haapi.service.sms.SmsContactService;

@Transactional
public class SmsContactSearchAndGroupInsertionIT extends FacadeITMockedThirdParties {
  @Autowired private UserRepository userRepository;
  @Autowired private SmsContactRepository smsContactRepository;
  @Autowired private SmsContactService smsContactService;
  @Autowired private SmsContactGroupService smsContactGroupService;

  private User aUser(String firstName, String lastName) {
    var id = UUID.randomUUID().toString();
    return User.builder()
        .id(id)
        .ref("REF" + id)
        .firstName(firstName)
        .lastName(lastName)
        .email("test+" + id + "@hei.school")
        .status(User.Status.ENABLED)
        .role(User.Role.STUDENT)
        .entranceDatetime(Instant.parse("2021-01-01T00:00:00Z"))
        .build();
  }

  private SmsContact aContact(User owner, String phoneNumber, String name) {
    return SmsContact.builder()
        .id(UUID.randomUUID().toString())
        .phoneNumber(phoneNumber)
        .name(name)
        .owner(owner)
        .ownerRole(SmsContactOwnerRole.STUDENT)
        .build();
  }

  @Test
  void finds_a_contact_by_owner_name_then_adds_only_that_one_to_a_group() {
    var uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
    var wantedName = "Rotamahafinaritra" + uniqueSuffix;
    var wanted =
        smsContactRepository.save(
            aContact(userRepository.save(aUser(wantedName, "Naina")), "0321111141", wantedName));
    smsContactRepository.save(
        aContact(
            userRepository.save(aUser("Tiana", "Rakoto" + uniqueSuffix)),
            "0321111142",
            "Tiana Rakoto" + uniqueSuffix));
    var group =
        smsContactGroupService.create(
            userRepository.save(aUser("Manager", "One")), "Test group", java.util.List.of());

    var found =
        smsContactService.getByCriteria(
            null, null, wantedName.toLowerCase(), PageRequest.of(0, 10));

    assertEquals(1, found.size());
    assertEquals(wanted.getId(), found.get(0).getId());

    var updatedGroup = smsContactGroupService.addMember(group.getId(), found.get(0).getId());

    assertEquals(1, updatedGroup.getMembers().size());
    assertEquals(wanted.getId(), updatedGroup.getMembers().get(0).getId());
  }

  @Test
  void finds_a_contact_by_phone_number_then_adds_only_that_one_to_a_group() {
    var uniquePhoneFragment = String.valueOf(System.nanoTime()).substring(0, 8);
    var wantedPhone = "032" + uniquePhoneFragment;
    var wanted =
        smsContactRepository.save(
            aContact(userRepository.save(aUser("Rota", "Naina")), wantedPhone, "Rota Naina"));
    smsContactRepository.save(
        aContact(userRepository.save(aUser("Tiana", "Rakoto")), "0321111199", "Tiana Rakoto"));
    var group =
        smsContactGroupService.create(
            userRepository.save(aUser("Manager", "Two")), "Test group 2", java.util.List.of());

    var found =
        smsContactService.getByCriteria(null, null, uniquePhoneFragment, PageRequest.of(0, 10));

    assertEquals(1, found.size());
    assertEquals(wanted.getId(), found.get(0).getId());

    var updatedGroup = smsContactGroupService.addMember(group.getId(), found.get(0).getId());

    assertTrue(updatedGroup.getMembers().stream().anyMatch(c -> c.getId().equals(wanted.getId())));
  }
}
