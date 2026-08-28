package school.hei.haapi.integration.testData;

import static java.util.UUID.randomUUID;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L1;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L2;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L3;

import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.model.Course;

public class CourseTestData {
  /** Stable within a run so a fixture built twice compares equal, but tied to no seeded row. */
  private static final String COURSE1_ID = randomUUID().toString();

  private static final String COURSE2_ID = randomUUID().toString();
  private static final String COURSE3_ID = randomUUID().toString();

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

  /** REST view of a model course, for the tests that assert on API payloads. */
  public static school.hei.haapi.endpoint.rest.model.Course toRest(
      Course course, StudentLevel level) {
    return new school.hei.haapi.endpoint.rest.model.Course()
        .id(course.getId())
        .code(course.getCode())
        .credits(course.getCredits())
        .totalHours(course.getTotalHours())
        .name(course.getName())
        .level(level);
  }
}
