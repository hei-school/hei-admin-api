package school.hei.haapi.unit;

import static java.time.Instant.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.SUCCESS;
import static school.hei.haapi.endpoint.rest.model.Payment.TypeEnum.MOBILE_MONEY;
import static school.hei.haapi.model.PaymentStatus.VALIDATE;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.Payment;
import school.hei.haapi.model.User;
import school.hei.haapi.model.mpbs.Mpbs;
import school.hei.haapi.repository.PaymentRepository;
import school.hei.haapi.service.PaymentService;

class PaymentFromMpbsTest {
  PaymentRepository paymentRepositoryMock = mock();
  EventProducer eventProducerMock = mock();
  PaymentService subject =
      new PaymentService(
          mock(), mock(), paymentRepositoryMock, mock(), mock(), eventProducerMock, mock(), mock());

  @Test
  void payment_created_from_an_mpbs_keeps_the_link_to_it() {
    var mpbs = verifiedMpbs();
    when(paymentRepositoryMock.findByMpbsId("mpbs1")).thenReturn(Optional.empty());
    when(paymentRepositoryMock.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

    var saved = subject.savePaymentFromMpbs(mpbs, 5000);

    assertSame(mpbs, saved.getMpbs());
    assertEquals(MOBILE_MONEY, saved.getType());
    assertEquals(VALIDATE, saved.getStatus());
    assertEquals(5000, saved.getAmount());
    assertEquals("feeId", saved.getFee().getId());
    verify(eventProducerMock, times(1)).accept(any());
  }

  @Test
  void an_mpbs_already_paid_creates_no_second_payment_and_no_second_notification() {
    var mpbs = verifiedMpbs();
    var alreadySaved = Payment.builder().id("payment1").build();
    when(paymentRepositoryMock.findByMpbsId("mpbs1")).thenReturn(Optional.of(alreadySaved));

    var returned = subject.savePaymentFromMpbs(mpbs, 5000);

    assertSame(alreadySaved, returned);
    verify(paymentRepositoryMock, never()).save(any(Payment.class));
    verify(eventProducerMock, never()).accept(any());
  }

  @Test
  void has_payment_from_mpbs_follows_the_link() {
    when(paymentRepositoryMock.findByMpbsId("paidMpbs"))
        .thenReturn(Optional.of(Payment.builder().id("payment1").build()));
    when(paymentRepositoryMock.findByMpbsId("unpaidMpbs")).thenReturn(Optional.empty());

    assertTrue(subject.hasPaymentFromMpbs("paidMpbs"));
    assertFalse(subject.hasPaymentFromMpbs("unpaidMpbs"));
  }

  private static Mpbs verifiedMpbs() {
    var student =
        User.builder()
            .id("studentId")
            .firstName("Axel")
            .lastName("HEI")
            .email("axel@hei.school")
            .build();
    var fee = Fee.builder().id("feeId").comment("Ecolage").student(student).build();
    return Mpbs.builder()
        .id("mpbs1")
        .pspId("MP260101.0000.B00000")
        .amount(5000)
        .status(SUCCESS)
        .student(student)
        .fee(fee)
        .creationDatetime(now())
        .statusHistory(List.of())
        .build();
  }
}
