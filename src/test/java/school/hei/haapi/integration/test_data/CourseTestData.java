package school.hei.haapi.integration.test_data;

import static java.util.UUID.randomUUID;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L1;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L2;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L3;

import school.hei.haapi.model.Course;

public class CourseTestData {
  public static final String COURSE1_ID = "course1_id";
  public static final String COURSE2_ID = "course2_id";
  public static final String COURSE3_ID = "course3_id";

  public static Course prog1() {
    return Course.builder()
        .id(randomUUID().toString())
        .code(randomUUID().toString())
        .name("Algorithmique")
        .credits(6)
        .studentLevel(L1)
        .totalHours(80)
        .build();
  }

  public static Course prog2() {
    return Course.builder()
        .id(randomUUID().toString())
        .code(randomUUID().toString())
        .name("Programmation Orientée-Objet")
        .credits(10)
        .studentLevel(L1)
        .totalHours(60)
        .build();
  }

  public static Course prog3() {
    return Course.builder()
        .id(randomUUID().toString())
        .code(randomUUID().toString())
        .name("Implémentation d'API Backend")
        .credits(6)
        .studentLevel(L2)
        .totalHours(80)
        .build();
  }

  public static Course prog4() {
    return Course.builder()
        .id(randomUUID().toString())
        .code(randomUUID().toString())
        .name("Advanced Programming")
        .credits(7)
        .studentLevel(L2)
        .totalHours(70)
        .build();
  }

  public static Course secu1() {
    return Course.builder()
        .id(randomUUID().toString())
        .code(randomUUID().toString())
        .name("Cybersecurity")
        .credits(6)
        .studentLevel(L3)
        .totalHours(30)
        .build();
  }

  public static Course course1Model() {
    return Course.builder()
        .id(COURSE1_ID)
        .code("PROG1")
        .credits(6)
        .totalHours(20)
        .name("Algorithmics")
        .build();
  }

  public static Course course2Model() {
    return Course.builder()
        .id(COURSE2_ID)
        .code("PROG3")
        .credits(6)
        .totalHours(24)
        .name("Advanced OOP")
        .build();
  }

  public static Course course3Model() {
    return Course.builder()
        .id(COURSE3_ID)
        .code("IA2")
        .credits(null)
        .totalHours(null)
        .name("Implemented IA")
        .build();
  }
}
