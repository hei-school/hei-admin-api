package school.hei.haapi.integration.testData;

import static java.util.UUID.randomUUID;
import static school.hei.haapi.endpoint.rest.model.FeeCategory.UNKNOWN;
import static school.hei.haapi.endpoint.rest.model.FeeFrequency.MONTHLY;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.UNPAID;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.TUITION;

import java.time.Instant;
import java.util.ArrayList;
import school.hei.haapi.endpoint.rest.model.FeeStatusEnum;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.FeeStatusHistory;
import school.hei.haapi.model.User;

public class FeeTestData {
  public static Fee createPendingFee(User student, Integer totalAmount, Instant dueDatetime) {
    return Fee.builder()
        .id(randomUUID().toString())
        .student(student)
        .type(TUITION)
        .frequency(MONTHLY)
        .totalAmount(totalAmount)
        .remainingAmount(totalAmount)
        .dueDatetime(dueDatetime)
        .creationDatetime(Instant.now())
        .status(UNPAID)
        .statusHistories(new ArrayList<>())
        .category(UNKNOWN)
        .build();
  }

  public static Fee createFeeWithStatus(
      User student, Integer totalAmount, Instant dueDatetime, FeeStatusEnum status) {
    return Fee.builder()
        .id(randomUUID().toString())
        .student(student)
        .type(TUITION)
        .frequency(MONTHLY)
        .totalAmount(totalAmount)
        .remainingAmount(totalAmount)
        .dueDatetime(dueDatetime)
        .creationDatetime(Instant.now())
        .status(status)
        .statusHistories(new ArrayList<>())
        .category(UNKNOWN)
        .build();
  }

  public static FeeStatusHistory createFeeStatusHistory(Fee fee, FeeStatusEnum status) {
    return FeeStatusHistory.builder().id(randomUUID().toString()).fee(fee).status(status).build();
  }
}
