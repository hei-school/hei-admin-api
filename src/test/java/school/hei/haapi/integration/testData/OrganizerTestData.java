package school.hei.haapi.integration.testData;

import static java.util.UUID.randomUUID;
import static school.hei.haapi.model.User.Role.ORGANIZER;
import static school.hei.haapi.model.User.Sex.F;
import static school.hei.haapi.model.User.Sex.M;
import static school.hei.haapi.model.User.Status.ENABLED;

import java.time.Instant;
import java.time.LocalDate;
import school.hei.haapi.model.User;

public class OrganizerTestData {
  public static User organizerSmith() {
    return User.builder()
        .id(randomUUID().toString())
        .ref("ORG" + randomUUID())
        .firstName("Organizer 1")
        .lastName("Smith")
        .email("test+" + randomUUID() + "@hei.school")
        .phone("0322400028")
        .status(ENABLED)
        .sex(M)
        .birthDate(LocalDate.parse("1980-10-10"))
        .entranceDatetime(Instant.parse("2022-09-08T08:25:29.00Z"))
        .address("Adr 10")
        .role(ORGANIZER)
        .build();
  }

  public static User organizerDoe() {
    return User.builder()
        .id(randomUUID().toString())
        .ref("ORG" + randomUUID())
        .firstName("Organizer 2")
        .lastName("Doe")
        .email("test+" + randomUUID() + "@hei.school")
        .phone("0322411113")
        .status(ENABLED)
        .sex(F)
        .birthDate(LocalDate.parse("1890-01-01"))
        .entranceDatetime(Instant.parse("2022-09-08T08:25:29.00Z"))
        .address("Adr 12")
        .role(ORGANIZER)
        .build();
  }
}
