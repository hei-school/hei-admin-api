package school.hei.haapi.integration.test_data;

import static java.util.UUID.randomUUID;
import static school.hei.haapi.model.User.Role.TEACHER;
import static school.hei.haapi.model.User.Status.DISABLED;
import static school.hei.haapi.model.User.Status.ENABLED;

import com.github.javafaker.Faker;
import java.time.Instant;
import school.hei.haapi.model.User;

public class TeacherTestData {
  public static User ryan() {
    return User.builder()
        .id(randomUUID().toString())
        .ref("TCR" + randomUUID())
        .firstName("Ryan")
        .lastName("HEI")
        .email(new Faker().internet().emailAddress())
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
        .email(new Faker().internet().emailAddress())
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
        .email(new Faker().internet().emailAddress())
        .phone("+261 34 12 345 04")
        .status(DISABLED)
        .entranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
        .role(TEACHER)
        .build();
  }
}
