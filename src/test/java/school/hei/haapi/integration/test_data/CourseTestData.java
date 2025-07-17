package school.hei.haapi.integration.test_data;

import java.util.UUID;
import school.hei.haapi.model.Course;

public class CourseTestData {
  public static Course prog1() {
    return Course.builder()
        .id(UUID.randomUUID().toString())
        .code("prog1")
        .name("Algorithmique")
        .credits(6)
        .totalHours(80)
        .build();
  }

  public static Course prog2() {
    return Course.builder()
        .id(UUID.randomUUID().toString())
        .code("prog2")
        .name("Programmation Orientée-Objet")
        .credits(10)
        .totalHours(60)
        .build();
  }

  public static Course prog3() {
    return Course.builder()
        .id(UUID.randomUUID().toString())
        .code("prog3")
        .name("Implémentation d'API Backend")
        .credits(6)
        .totalHours(80)
        .build();
  }

  public static Course prog4() {
    return Course.builder()
        .id(UUID.randomUUID().toString())
        .code("prog4")
        .name("Advanced Programming")
        .credits(7)
        .totalHours(70)
        .build();
  }
}
