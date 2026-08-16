package school.hei.haapi.integration.testData;

import static java.util.UUID.randomUUID;
import static school.hei.haapi.model.User.Role.ADMIN;
import static school.hei.haapi.model.User.Role.STAFF_MEMBER;
import static school.hei.haapi.model.User.Status.ENABLED;

import java.time.Instant;
import school.hei.haapi.model.User;

public class StaffTestData {
  public static User staffMemberRina() {
    return User.builder()
        .id(randomUUID().toString())
        .ref("STF" + randomUUID())
        .firstName("Rina")
        .lastName("HEI")
        .email("test+" + randomUUID() + "@hei.school")
        .phone("+261 34 12 345 30")
        .status(ENABLED)
        .entranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
        .role(STAFF_MEMBER)
        .build();
  }

  public static User adminMialy() {
    return User.builder()
        .id(randomUUID().toString())
        .ref("ADM" + randomUUID())
        .firstName("Mialy")
        .lastName("HEI")
        .email("test+" + randomUUID() + "@hei.school")
        .phone("+261 34 12 345 31")
        .status(ENABLED)
        .entranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
        .role(ADMIN)
        .build();
  }
}
