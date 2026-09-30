package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.SmsContact;
import school.hei.haapi.model.SmsContactOwnerRole;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.SmsContactRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.UserService;

public class UserWithoutSmsContactIT extends FacadeITMockedThirdParties {
  @Autowired private UserRepository userRepository;
  @Autowired private SmsContactRepository smsContactRepository;
  @Autowired private UserService userService;

  private User anEnabledUser() {
    var id = UUID.randomUUID().toString();
    return User.builder()
        .id(id)
        .ref("REF" + id)
        .firstName("Test")
        .lastName("User")
        .email("test+" + id + "@hei.school")
        .status(User.Status.ENABLED)
        .role(User.Role.STUDENT)
        .entranceDatetime(Instant.parse("2021-01-01T00:00:00Z"))
        .build();
  }

  @Test
  void getAllEnabledUsersWithoutContact_excludes_users_that_already_have_a_contact() {
    var withoutContact = userRepository.save(anEnabledUser());
    var withContact = userRepository.save(anEnabledUser());
    smsContactRepository.save(
        SmsContact.builder()
            .id(UUID.randomUUID().toString())
            .phoneNumber("0321111151")
            .name("Test Contact")
            .owner(withContact)
            .ownerRole(SmsContactOwnerRole.STUDENT)
            .build());
    var disabled =
        userRepository.save(anEnabledUser().toBuilder().status(User.Status.DISABLED).build());

    var result = userService.getAllEnabledUsersWithoutContact();

    assertTrue(result.stream().anyMatch(u -> u.getId().equals(withoutContact.getId())));
    assertTrue(result.stream().noneMatch(u -> u.getId().equals(withContact.getId())));
    assertTrue(result.stream().noneMatch(u -> u.getId().equals(disabled.getId())));
  }
}
