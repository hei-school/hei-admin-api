package school.hei.haapi.model;

import static org.junit.jupiter.api.Assertions.*;
import static school.hei.haapi.endpoint.rest.model.FeeCategory.L1;
import static school.hei.haapi.endpoint.rest.model.FeeFrequency.MONTHLY;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.PENDING;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.TUITION;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class FeeTest {
  @Test
  void equals_test() {
    Fee fee1 =
        Fee.builder().id("0000").student(new User()).totalAmount(0).remainingAmount(0).build();
    Fee fee2 =
        Fee.builder().id("0001").student(new User()).totalAmount(100).remainingAmount(0).build();

    assertEquals(fee1, fee1);
    assertNotEquals(fee1, fee2);
    assertNotEquals(null, fee1);
  }

  @Test
  void hashCode_test() {
    User student = new User();
    Fee fee1 =
        Fee.builder()
            .id("0000")
            .comment("comment")
            .student(student)
            .totalAmount(0)
            .remainingAmount(0)
            .status(PENDING)
            .updatedAt(Instant.now())
            .creationDatetime(Instant.now())
            .dueDatetime(Instant.now())
            .category(L1)
            .frequency(MONTHLY)
            .type(TUITION)
            .build();
    Fee fee2 =
        Fee.builder()
            .id("0001")
            .comment("comment")
            .student(student)
            .totalAmount(100)
            .remainingAmount(0)
            .status(PENDING)
            .updatedAt(Instant.now())
            .creationDatetime(Instant.now())
            .dueDatetime(Instant.now())
            .category(L1)
            .frequency(MONTHLY)
            .type(TUITION)
            .build();

    assertEquals(fee1.hashCode(), fee1.hashCode());
    assertNotEquals(fee1.hashCode(), fee2.hashCode());
    assertNotEquals(0, fee1.hashCode());
  }
}
