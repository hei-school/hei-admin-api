package school.hei.haapi.integration.utils;

import java.util.UUID;
import school.hei.haapi.model.Group;

public class GroupUtils {
  public static Group g1() {
    return Group.builder()
        .id(UUID.randomUUID().toString())
        .name("G1")
        .ref("G1")
        .attributedColor("green")
        .build();
  }

  public static Group g2() {
    return Group.builder()
        .id(UUID.randomUUID().toString())
        .name("G2")
        .ref("G2")
        .attributedColor("blue")
        .build();
  }

  public static Group g3() {
    return Group.builder()
        .id(UUID.randomUUID().toString())
        .name("G3")
        .ref("G3")
        .attributedColor("red")
        .build();
  }
}
