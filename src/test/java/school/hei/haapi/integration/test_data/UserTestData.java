package school.hei.haapi.integration.test_data;

import school.hei.haapi.model.User;

public class UserTestData {
  public static User user1() {
    return User.builder()
        .id("student1_id")
        .email("email1")
        .firstName("firstName1")
        .lastName("lastName1")
        .address("address1")
        .cnaps("cnaps")
        .build();
  }
}
