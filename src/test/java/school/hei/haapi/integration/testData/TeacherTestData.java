package school.hei.haapi.integration.testData;

import static java.util.UUID.randomUUID;
import static school.hei.haapi.model.User.Role.TEACHER;
import static school.hei.haapi.model.User.Sex.F;
import static school.hei.haapi.model.User.Status.DISABLED;
import static school.hei.haapi.model.User.Status.ENABLED;
import static school.hei.haapi.model.User.Status.SUSPENDED;

import java.time.Instant;
import java.time.LocalDate;
import school.hei.haapi.model.User;

public class TeacherTestData {
  public static User ryan() {
    return User.builder()
        .id(randomUUID().toString())
        .ref("TCR" + randomUUID())
        .firstName("Ryan")
        .lastName("HEI")
        .email("test+" + randomUUID() + "@hei.school")
        .phone("+261 34 12 345 02")
        .status(ENABLED)
        .entranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
        .role(TEACHER)
        .build();
  }

  public static User toky() {
    return User.builder()
        .id(randomUUID().toString())
        .ref("TCR" + randomUUID())
        .firstName("Toky")
        .lastName("HEI")
        .email("test+" + randomUUID() + "@hei.school")
        .phone("+261 34 12 345 03")
        .status(ENABLED)
        .entranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
        .role(TEACHER)
        .build();
  }

  public static User harry() {
    return User.builder()
        .id(randomUUID().toString())
        .ref("TCR" + randomUUID())
        .firstName("Harry")
        .lastName("HEI")
        .email("test+" + randomUUID() + "@hei.school")
        .phone("+261 34 12 345 04")
        .status(DISABLED)
        .entranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
        .role(TEACHER)
        .build();
  }

  public static User disabledFemaleTeacher() {
    return User.builder()
        .id(randomUUID().toString())
        .ref("TCR" + randomUUID())
        .firstName("Disabled")
        .lastName("HEI")
        .email("test+" + randomUUID() + "@hei.school")
        .phone("+261 34 12 345 05")
        .status(DISABLED)
        .sex(F)
        .birthDate(LocalDate.parse("2000-12-01"))
        .entranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
        .role(TEACHER)
        .build();
  }

  public static User suspendedFemaleTeacher() {
    return User.builder()
        .id(randomUUID().toString())
        .ref("TCR" + randomUUID())
        .firstName("Suspended")
        .lastName("HEI")
        .email("test+" + randomUUID() + "@hei.school")
        .phone("+261 34 12 345 06")
        .status(SUSPENDED)
        .sex(F)
        .birthDate(LocalDate.parse("2000-12-02"))
        .entranceDatetime(Instant.parse("2021-11-09T08:26:24.00Z"))
        .role(TEACHER)
        .build();
  }
}
