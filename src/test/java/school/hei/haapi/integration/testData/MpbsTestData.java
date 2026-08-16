package school.hei.haapi.integration.testData;

import static school.hei.haapi.endpoint.rest.model.MobileMoneyType.ORANGE_MONEY;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.PENDING;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;
import school.hei.haapi.endpoint.rest.model.CrupdateMpbs;
import school.hei.haapi.endpoint.rest.model.MobileMoneyType;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.User;
import school.hei.haapi.model.mpbs.Mpbs;

public class MpbsTestData {
  public static Mpbs createPendingMpbs(String pspId, User student, Fee fee, int amount) {
    return Mpbs.builder()
        .pspId(pspId)
        .mobileMoneyType(ORANGE_MONEY)
        .amount(amount)
        .status(PENDING)
        .student(student)
        .fee(fee)
        .statusHistory(new ArrayList<>())
        .creationDatetime(Instant.now())
        .build();
  }

  public static CrupdateMpbs createCrupdateMpbs(
      String studentId, String feeId, String pspId, MobileMoneyType mobileMoneyType) {
    return new CrupdateMpbs()
        .id(UUID.randomUUID().toString())
        .studentId(studentId)
        .feeId(feeId)
        .pspId(pspId)
        .pspType(mobileMoneyType);
  }

  public static CrupdateMpbs createableMpbsFromFeeIdForStudent(String studentId, String feeId) {
    return createCrupdateMpbs(studentId, feeId, "MP240726.1541.D88425", ORANGE_MONEY);
  }
}
