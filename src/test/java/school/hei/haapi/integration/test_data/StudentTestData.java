package school.hei.haapi.integration.test_data;

import static java.util.UUID.randomUUID;

import com.github.javafaker.Faker;
import java.time.Instant;
import java.util.ArrayList;
import school.hei.haapi.model.User;

public class StudentTestData {
  public static User axel() {
    return User.builder()
        .id(randomUUID().toString())
        .firstName("Axel")
        .lastName("HEI")
        .email(new Faker().internet().emailAddress())
        .ref("STD" + randomUUID())
        .phone("+261 34 94 543 21")
        .address("123 Avenue de l'Indépendance")
        .role(User.Role.STUDENT)
        .status(User.Status.ENABLED)
        .entranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
        .groupFlows(new ArrayList<>())
        .build();
  }

  public static User freddy() {
    return User.builder()
        .id(randomUUID().toString())
        .firstName("Freddy")
        .lastName("HEI")
        .email(new Faker().internet().emailAddress())
        .ref("STD" + randomUUID())
        .phone("+261 34 45 672 10")
        .address("123 Avenue Rasseta")
        .role(User.Role.STUDENT)
        .status(User.Status.ENABLED)
        .entranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
        .groupFlows(new ArrayList<>())
        .build();
  }

  public static User tolojanahary() {
    return User.builder()
        .id(randomUUID().toString())
        .firstName("Tolojanahary")
        .lastName("HEI")
        .email(new Faker().internet().emailAddress())
        .ref("STD" + randomUUID())
        .phone("+261 34 83 765 43")
        .address("456 Rue de la République")
        .role(User.Role.STUDENT)
        .status(User.Status.ENABLED)
        .entranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
        .groupFlows(new ArrayList<>())
        .build();
  }

  public static User manitra() {
    return User.builder()
        .id(randomUUID().toString())
        .firstName("Manitra")
        .lastName("HEI")
        .email(new Faker().internet().emailAddress())
        .ref("STD" + randomUUID())
        .phone("+261 32 88 715 43")
        .address("456 Rue de la République")
        .role(User.Role.STUDENT)
        .status(User.Status.ENABLED)
        .entranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
        .groupFlows(new ArrayList<>())
        .build();
  }
}
