package school.hei.haapi.integration.testData;

import static java.util.UUID.randomUUID;

import java.time.Instant;
import school.hei.haapi.endpoint.rest.model.MobileMoneyType;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.User;
import school.hei.haapi.model.mpbs.MpbsVerification;

public class MpbsVerificationTestData {
  public static MpbsVerification aMpbsVerification(
      User student, Fee fee, MobileMoneyType type, int amount, Instant paymentDatetime) {
    return MpbsVerification.builder()
        .id(randomUUID().toString())
        .pspId("psp_" + randomUUID())
        .mobileMoneyType(type)
        .student(student)
        .fee(fee)
        .amountInPsp(amount)
        .amountOfFeeRemainingPayment(amount)
        .comment("comment 1")
        .creationDatetimeOfPaymentInPsp(paymentDatetime)
        .creationDatetimeOfMpbs(paymentDatetime)
        .build();
  }
}
