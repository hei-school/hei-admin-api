package school.hei.haapi.integration.test_data;

import static java.util.UUID.randomUUID;

import java.time.Instant;
import school.hei.haapi.model.Group;

public class GroupTestData {
  public static Group g1() {
    return Group.builder()
        .id(randomUUID().toString())
        .name("G1")
        .ref(randomUUID().toString())
        .attributedColor("green")
        .creationDatetime(Instant.now())
        .build();
  }

  public static Group g2() {
    return Group.builder()
        .id(randomUUID().toString())
        .name("G2")
        .ref(randomUUID().toString())
        .attributedColor("blue")
        .creationDatetime(Instant.now())
        .build();
  }
}
