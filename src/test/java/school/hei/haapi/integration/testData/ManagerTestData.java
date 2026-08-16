package school.hei.haapi.integration.testData;

import static java.util.UUID.randomUUID;
import static school.hei.haapi.model.User.Role.MANAGER;
import static school.hei.haapi.model.User.Status.ENABLED;

import java.time.Instant;
import school.hei.haapi.model.User;

public class ManagerTestData {
  public static User hasina() {
    return User.builder()
        .id(randomUUID().toString())
        .ref("MGR" + randomUUID())
        .firstName("Hasina")
        .lastName("HEI")
        .email("test+" + randomUUID() + "@hei.school")
        .phone("+261 34 12 345 20")
        .status(ENABLED)
        .entranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
        .role(MANAGER)
        .build();
  }

  public static User njiva() {
    return User.builder()
        .id(randomUUID().toString())
        .ref("MGR" + randomUUID())
        .firstName("Njiva")
        .lastName("HEI")
        .email("test+" + randomUUID() + "@hei.school")
        .phone("+261 34 12 345 21")
        .status(ENABLED)
        .entranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
        .role(MANAGER)
        .build();
  }
}
