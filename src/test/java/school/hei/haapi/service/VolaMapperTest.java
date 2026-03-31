package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static school.hei.haapi.endpoint.rest.model.MobileMoneyType.AIRTEL_MONEY;
import static school.hei.haapi.endpoint.rest.model.MobileMoneyType.ORANGE_MONEY;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import school.hei.haapi.endpoint.rest.mapper.VolaMapper;
import school.hei.haapi.endpoint.rest.model.MobileMoneyType;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.User;
import school.hei.haapi.model.mpbs.Mpbs;
import school.hei.haapi.model.psp.vola.api.gen.client.model.Payment;
import school.hei.haapi.model.psp.vola.api.gen.client.model.PspPayment;

class VolaMapperTest {

  private VolaMapper volaMapper;

  @BeforeEach
  void setUp() {
    volaMapper = new VolaMapper();
  }

  @Test
  void toPspType_shouldReturnOrangeMoney_whenMobileMoneyTypeIsOrangeMoney() {
    MobileMoneyType mobileMoneyType = ORANGE_MONEY;
    var result = volaMapper.toPspType(mobileMoneyType);
    assertEquals(PspPayment.PspTypeEnum.ORANGE_MONEY, result);
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
    var pspType = PspPayment.PspTypeEnum.ORANGE_MONEY;
    MobileMoneyType result = volaMapper.toMobilePaymentType(pspType);
    assertEquals(ORANGE_MONEY, result);
  }

  @Test
  void toMpbs_shouldMapCorrectly_whenVolaPaymentIsConfirmed() {
    var student = Mockito.mock(User.class);
    var fee = Mockito.mock(Fee.class);
    var statusHistory = List.of(new school.hei.haapi.model.mpbs.MpbsStatusHistory());

    var verificationInstant = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    var pspPayment =
        PspPayment.builder()
            .pspType(PspPayment.PspTypeEnum.ORANGE_MONEY)
            .id("psp123")
            .amount(1500)
            .creationInstant(verificationInstant.atOffset(java.time.ZoneOffset.UTC))
            .build();
    var payer =
        school.hei.haapi.model.psp.vola.api.gen.client.model.User.builder()
            .email("dummy@gmail.com")
            .build();
    var volaPayment =
        Payment.builder()
            .id("p1")
            .pspPayment(pspPayment)
            .creationInstant(verificationInstant.atOffset(java.time.ZoneOffset.UTC))
            .lastPspVerificationInstant(verificationInstant.atOffset(java.time.ZoneOffset.UTC))
            .verificationAttemptNb(1)
            .payer(payer)
            .verificationStatus(Payment.VerificationStatusEnum.SUCCEEDED)
            .build();

    var result = volaMapper.toMpbs(volaPayment, "mpbsId", student, fee, statusHistory);

    assertNotNull(result);
    assertEquals("mpbsId", result.getId());
    assertEquals(student, result.getStudent());
    assertEquals(fee, result.getFee());
    assertEquals(volaPayment.getPspPayment().getAmount(), result.getAmount());
    assertEquals(verificationInstant, result.getSuccessfullyVerifiedOn());
    assertEquals(verificationInstant, result.getPspOwnDatetimeVerification());
    assertEquals(
        volaPayment.getLastPspVerificationInstant().toInstant(),
        result.getLastVerificationDatetime());
    assertEquals(ORANGE_MONEY, result.getMobileMoneyType());
    assertEquals(school.hei.haapi.endpoint.rest.model.MpbsStatus.SUCCESS, result.getStatus());
    assertEquals(volaPayment.getCreationInstant().toInstant(), result.getCreationDatetime());
    assertEquals(statusHistory, result.getStatusHistory());
  }

  @Test
  void toMpbs_shouldMapCorrectly_whenVolaPaymentIsRefused() {
    var student = Mockito.mock(User.class);
    var fee = Mockito.mock(Fee.class);

    var pspPayment =
        PspPayment.builder()
            .pspType(PspPayment.PspTypeEnum.ORANGE_MONEY)
            .id("psp123")
            .amount(1500)
            .creationInstant(Instant.now().atOffset(java.time.ZoneOffset.UTC))
            .build();
    var payer =
        school.hei.haapi.model.psp.vola.api.gen.client.model.User.builder()
            .email("dummy@gmail.com")
            .build();
    var volaPayment =
        Payment.builder()
            .id("p1")
            .pspPayment(pspPayment)
            .creationInstant(Instant.now().atOffset(java.time.ZoneOffset.UTC))
            .lastPspVerificationInstant(Instant.now().atOffset(java.time.ZoneOffset.UTC))
            .verificationAttemptNb(1)
            .payer(payer)
            .verificationStatus(Payment.VerificationStatusEnum.FAILED)
            .build();

    Mpbs result = volaMapper.toMpbs(volaPayment, "mpbsId", student, fee, List.of());

    assertNotNull(result);
    assertEquals(school.hei.haapi.endpoint.rest.model.MpbsStatus.FAILED, result.getStatus());
    assertNull(result.getSuccessfullyVerifiedOn());
    assertNull(result.getPspOwnDatetimeVerification());
  }

  @Test
  void toMpbs_shouldMapCorrectly_whenVolaPaymentIsVerifying() {
    var student = Mockito.mock(User.class);
    var fee = Mockito.mock(Fee.class);

    var pspPayment =
        PspPayment.builder()
            .pspType(PspPayment.PspTypeEnum.ORANGE_MONEY)
            .id("psp123")
            .amount(1500)
            .creationInstant(Instant.now().atOffset(java.time.ZoneOffset.UTC))
            .build();
    var payer =
        school.hei.haapi.model.psp.vola.api.gen.client.model.User.builder()
            .email("dummy@gmail.com")
            .build();
    var volaPayment =
        Payment.builder()
            .id("p1")
            .pspPayment(pspPayment)
            .creationInstant(Instant.now().atOffset(java.time.ZoneOffset.UTC))
            .lastPspVerificationInstant(Instant.now().atOffset(java.time.ZoneOffset.UTC))
            .verificationAttemptNb(1)
            .payer(payer)
            .verificationStatus(Payment.VerificationStatusEnum.VERIFYING)
            .build();

    Mpbs result = volaMapper.toMpbs(volaPayment, "mpbsId", student, fee, List.of());

    assertNotNull(result);
    assertEquals(school.hei.haapi.endpoint.rest.model.MpbsStatus.PENDING, result.getStatus());
    assertNull(result.getSuccessfullyVerifiedOn());
    assertNull(result.getPspOwnDatetimeVerification());
  }
}
