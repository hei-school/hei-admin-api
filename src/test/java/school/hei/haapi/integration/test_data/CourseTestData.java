package school.hei.haapi.integration.test_data;

import school.hei.haapi.model.Course;
import static java.util.UUID.randomUUID;

public class CourseTestData {
  public static Course prog1() {
    return Course.builder()
        .id(randomUUID().toString())
        .code(randomUUID().toString())
        .name("Algorithmique")
        .credits(6)
        .totalHours(80)
        .build();
  }

  public static Course prog2() {
    return Course.builder()
        .id(randomUUID().toString())
        .code(randomUUID().toString())
        .name("Programmation Orientée-Objet")
        .credits(10)
        .totalHours(60)
        .build();
  }

  public static Course prog3() {
    return Course.builder()
        .id(randomUUID().toString())
        .code(randomUUID().toString())
        .name("Implémentation d'API Backend")
        .credits(6)
        .totalHours(80)
        .build();
  }

  public static Course prog4() {
    return Course.builder()
        .id(randomUUID().toString())
        .code(randomUUID().toString())
        .name("Advanced Programming")
        .credits(7)
        .totalHours(70)
        .build();
  }
}
