package school.hei.haapi.integration.test_data;

import static java.util.UUID.randomUUID;

import java.time.Instant;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.User;

public class GroupTestData {
  public static Group g1() {
    return Group.builder()
        .id(randomUUID().toString())
        .name("G1")
        .ref(randomUUID().toString())
        .attributedColor("green")
        .creationDatetime(Instant.parse("2025-07-28T10:00:00Z"))
        .build();
  }

  public static Group g2() {
    return Group.builder()
        .id(randomUUID().toString())
        .name("G2")
        .ref(randomUUID().toString())
        .attributedColor("blue")
        .creationDatetime(Instant.parse("2025-07-28T11:00:00Z"))
        .build();
  }

  public static GroupFlow createGroupFlow(User student, Group toJoin) {
    return GroupFlow.builder()
        .id(randomUUID().toString())
        .group(toJoin)
        .flowDatetime(Instant.parse("2025-07-28T12:00:00Z"))
        .student(student)
        .groupFlowType(GroupFlow.GroupFlowType.JOIN)
        .build();
  }
}
