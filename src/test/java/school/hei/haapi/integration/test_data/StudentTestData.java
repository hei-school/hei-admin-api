package school.hei.haapi.integration.test_data;

import static java.util.UUID.randomUUID;

import java.time.Instant;
import java.util.ArrayList;
import school.hei.haapi.model.User;

public class StudentTestData {
  public static User axel() {
    return User.builder()
        .id(randomUUID().toString())
        .firstName("Axel")
        .lastName("HEI")
        .email("axel.dev@hei.school")
        .ref("STD" + randomUUID())
        .phone("+261 34 94 543 21")
        .address("123 Avenue de l'Indépendance")
        .role(User.Role.STUDENT)
        .status(User.Status.ENABLED)
        .entranceDatetime(Instant.now())
        .groupFlows(new ArrayList<>())
        .build();
  }

  public static User tolojanahary() {
    return User.builder()
        .id(randomUUID().toString())
        .firstName("Tolojanahary")
        .lastName("HEI")
        .email("tolojanahary@hei.school")
        .ref("STD" + randomUUID())
        .phone("+261 34 83 765 43")
        .address("456 Rue de la République")
        .role(User.Role.STUDENT)
        .status(User.Status.ENABLED)
        .entranceDatetime(Instant.now())
        .groupFlows(new ArrayList<>())
        .build();
  }
}
