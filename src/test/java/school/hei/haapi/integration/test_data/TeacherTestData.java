package school.hei.haapi.integration.test_data;

import com.github.javafaker.Faker;
import java.time.Instant;
import java.util.UUID;
import school.hei.haapi.model.User;

public class TeacherTestData {
  public static User ryan() {
    return User.builder()
        .id(UUID.randomUUID().toString())
        .ref("TCR" + UUID.randomUUID())
        .firstName("Ryan")
        .lastName("HEI")
        .email(new Faker().internet().emailAddress())
        .phone("+261 34 12 345 02")
        .status(User.Status.ENABLED)
        .entranceDatetime(Instant.now())
        .role(User.Role.TEACHER)
        .build();
  }

  public static User toky() {
    return User.builder()
        .id(UUID.randomUUID().toString())
        .ref("TCR" + UUID.randomUUID())
        .firstName("Toky")
        .lastName("HEI")
        .email(new Faker().internet().emailAddress())
        .phone("+261 34 12 345 03")
        .status(User.Status.ENABLED)
        .entranceDatetime(Instant.now())
        .role(User.Role.TEACHER)
        .build();
  }

  public static User harry() {
    return User.builder()
        .id(UUID.randomUUID().toString())
        .ref("TCR" + UUID.randomUUID())
        .firstName("Harry")
        .lastName("HEI")
        .email(new Faker().internet().emailAddress())
        .phone("+261 34 12 345 04")
        .status(User.Status.DISABLED)
        .entranceDatetime(Instant.now())
        .role(User.Role.TEACHER)
        .build();
  }
}
