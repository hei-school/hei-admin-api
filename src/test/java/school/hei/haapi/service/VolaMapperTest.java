package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static school.hei.haapi.endpoint.rest.model.MobileMoneyType.AIRTEL_MONEY;
import static school.hei.haapi.endpoint.rest.model.MobileMoneyType.ORANGE_MONEY;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.PENDING;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import school.hei.haapi.endpoint.rest.mapper.VolaMapper;
import school.hei.haapi.endpoint.rest.model.MobileMoneyType;
import school.hei.haapi.model.PaymentStatus;
import school.hei.haapi.model.VolaPayment;
import school.hei.haapi.model.mpbs.Mpbs;
import school.hei.haapi.model.psp.PspType;

class VolaMapperTest {

  private VolaMapper volaMapper;

  @BeforeEach
  void setUp() {
    volaMapper = new VolaMapper();
  }

  @Test
  void toPspType_shouldReturnOrangeMoney_whenMobileMoneyTypeIsOrangeMoney() {
    MobileMoneyType mobileMoneyType = ORANGE_MONEY;
    PspType result = volaMapper.toPspType(mobileMoneyType);
    assertEquals(PspType.ORANGE_MONEY, result);
  }

  @Test
  void toPspType_shouldThrowRuntimeException_whenMobileMoneyTypeIsNotSupported() {
    MobileMoneyType mobileMoneyType = AIRTEL_MONEY;
    assertThrows(
        school.hei.haapi.model.exception.UnsupportedPspTypeException.class,
        () -> volaMapper.toPspType(mobileMoneyType));
  }

  @Test
  void toMobilePaymentType_shouldReturnOrangeMoney_whenPspTypeIsOrangeMoney() {
    PspType pspType = PspType.ORANGE_MONEY;
    MobileMoneyType result = volaMapper.toMobilePaymentType(pspType);
    assertEquals(ORANGE_MONEY, result);
  }

  @Test
  void toMpbs_shouldMapCorrectly_whenVolaPaymentIsConfirmed() {
    var mpbs =
        Mpbs.builder()
            .id("mpbsId")
            .student(Mockito.mock(school.hei.haapi.model.User.class))
            .fee(Mockito.mock(school.hei.haapi.model.Fee.class))
            .amount(1000)
            .creationDatetime(Instant.now())
            .build();

    var verificationInstant = Instant.now();
    var volaPayment =
        VolaPayment.builder()
            .pspId("psp123")
            .amount(1500)
            .creationInstant(verificationInstant.minusSeconds(3600))
            .pspLastVerificationInstant(verificationInstant)
            .status(PaymentStatus.CONFIRMED)
            .pspType(PspType.ORANGE_MONEY)
            .build();

    var result = volaMapper.toMpbs(mpbs, volaPayment);

    assertNotNull(result);
    assertEquals(mpbs.getId(), result.getId());
    assertEquals(mpbs.getStudent(), result.getStudent());
    assertEquals(mpbs.getFee(), result.getFee());
    assertEquals(volaPayment.amount(), result.getAmount());
    assertEquals(verificationInstant, result.getSuccessfullyVerifiedOn());
    assertEquals(verificationInstant, result.getPspOwnDatetimeVerification());
    assertEquals(volaPayment.pspLastVerificationInstant(), result.getLastVerificationDatetime());
    assertEquals(ORANGE_MONEY, result.getMobileMoneyType());
    assertEquals(school.hei.haapi.endpoint.rest.model.MpbsStatus.SUCCESS, result.getStatus());
    assertEquals(volaPayment.creationInstant(), result.getCreationDatetime());
    assertEquals(mpbs.getStatusHistory(), result.getStatusHistory());
  }

  @Test
  void toMpbs_shouldMapCorrectly_whenVolaPaymentIsRefused() {
    Mpbs mpbs =
        Mpbs.builder()
            .id("mpbsId")
            .student(Mockito.mock(school.hei.haapi.model.User.class))
            .fee(Mockito.mock(school.hei.haapi.model.Fee.class))
            .amount(1000)
            .creationDatetime(Instant.now())
            .build();

    VolaPayment volaPayment =
        VolaPayment.builder()
            .pspId("psp123")
            .amount(1500)
            .creationInstant(Instant.now())
            .pspLastVerificationInstant(Instant.now())
            .status(PaymentStatus.REFUSED)
            .pspType(PspType.ORANGE_MONEY)
            .build();

    Mpbs result = volaMapper.toMpbs(mpbs, volaPayment);

    assertNotNull(result);
    assertEquals(school.hei.haapi.endpoint.rest.model.MpbsStatus.FAILED, result.getStatus());
    assertNull(result.getSuccessfullyVerifiedOn());
    assertNull(result.getPspOwnDatetimeVerification());
  }

  @Test
  void toMpbs_shouldMapCorrectly_whenVolaPaymentIsVerifying() {
    Mpbs mpbs =
        Mpbs.builder()
            .id("mpbsId")
            .student(Mockito.mock(school.hei.haapi.model.User.class))
            .fee(Mockito.mock(school.hei.haapi.model.Fee.class))
            .amount(1000)
            .creationDatetime(Instant.now())
            .build();

    VolaPayment volaPayment =
        VolaPayment.builder()
            .pspId("psp123")
            .amount(1500)
            .creationInstant(Instant.now())
            .pspLastVerificationInstant(Instant.now())
            .status(PaymentStatus.VERIFYING)
            .pspType(PspType.ORANGE_MONEY)
            .build();

    Mpbs result = volaMapper.toMpbs(mpbs, volaPayment);

    assertNotNull(result);
    assertEquals(school.hei.haapi.endpoint.rest.model.MpbsStatus.PENDING, result.getStatus());
    assertNull(result.getSuccessfullyVerifiedOn());
    assertNull(result.getPspOwnDatetimeVerification());
  }

  @Test
  void toMpbs_shouldMapToPending_whenVolaPaymentIsUnknown() {
    Mpbs mpbs =
        Mpbs.builder()
            .id("mpbsId")
            .student(Mockito.mock(school.hei.haapi.model.User.class))
            .fee(Mockito.mock(school.hei.haapi.model.Fee.class))
            .amount(1000)
            .creationDatetime(Instant.now())
            .build();

    VolaPayment volaPayment =
        VolaPayment.builder()
            .pspId("psp123")
            .amount(1500)
            .creationInstant(Instant.now())
            .pspLastVerificationInstant(Instant.now())
            .status(PaymentStatus.UNKNOWN)
            .pspType(PspType.ORANGE_MONEY)
            .build();

    Mpbs result = volaMapper.toMpbs(mpbs, volaPayment);

    assertNotNull(result);
    assertEquals(PENDING, result.getStatus());
    assertNull(result.getSuccessfullyVerifiedOn());
    assertNull(result.getPspOwnDatetimeVerification());
  }
}
