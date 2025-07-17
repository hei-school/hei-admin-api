package school.hei.haapi.integration.test_data;

import java.util.Random;
import java.util.UUID;
import school.hei.haapi.model.Group;

public class GroupTestData {
  public static Group g1() {
    return Group.builder()
        .id(UUID.randomUUID().toString())
        .name("G1")
        .ref(UUID.randomUUID().toString())
        .attributedColor("green")
        .build();
  }

  public static Group g2() {
    return Group.builder()
        .id(UUID.randomUUID().toString())
        .name("G2")
        .ref(UUID.randomUUID().toString())
        .attributedColor("blue")
        .build();
  }
}
