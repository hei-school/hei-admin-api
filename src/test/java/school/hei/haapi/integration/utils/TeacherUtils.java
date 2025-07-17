package school.hei.haapi.integration.utils;

import java.util.UUID;
import school.hei.haapi.model.User;

public class TeacherUtils {
  public static User lou() {
    return User.builder()
        .id(UUID.randomUUID().toString())
        .ref("REF-TEACHER-001")
        .firstName("Lou")
        .lastName("HEI")
        .email("john.smith@hei.school")
        .phone("+261 34 12 345 01")
        .status(User.Status.ENABLED)
        .build();
  }

  public static User ryan() {
    return User.builder()
        .id(UUID.randomUUID().toString())
        .ref("REF-TEACHER-002")
        .firstName("Ryan")
        .lastName("HEI")
        .email("jane.doe@hei.school")
        .phone("+261 34 12 345 02")
        .status(User.Status.ENABLED)
        .build();
  }

  public static User toky() {
    return User.builder()
        .id(UUID.randomUUID().toString())
        .ref("REF-TEACHER-003")
        .firstName("Toky")
        .lastName("HEI")
        .email("bob.wilson@hei.school")
        .phone("+261 34 12 345 03")
        .status(User.Status.ENABLED)
        .build();
  }

  public static User harry() {
    return User.builder()
        .id(UUID.randomUUID().toString())
        .ref("REF-TEACHER-004")
        .firstName("Harry")
        .lastName("HEI")
        .email("harry@something.com")
        .phone("+261 34 12 345 04")
        .status(User.Status.DISABLED)
        .build();
  }
}
