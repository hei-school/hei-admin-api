package school.hei.haapi.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.SUCCESS;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import school.hei.haapi.http.model.TransactionDetails;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.Payment;
import school.hei.haapi.model.exception.NoRemainingAmountFee;
import school.hei.haapi.model.mpbs.Mpbs;
import school.hei.haapi.repository.MpbsRepository;
import school.hei.haapi.service.ComputeVerifiedMobilePayment;
import school.hei.haapi.service.FeeService;
import school.hei.haapi.service.MpbsService;
import school.hei.haapi.service.PaymentService;

class ComputeVerifiedMobilePaymentTest {
  private final MpbsService mpbsServiceMock = mock();
  private final MpbsRepository mpbsRepositoryMock = mock();
  private final PaymentService paymentServiceMock = mock();
  private final FeeService feeServiceMock =
      new FeeService(
          mock(), mock(), mock(), mock(), mock(), mock(), mock(), mock(), mock(), mock());
  private final ComputeVerifiedMobilePayment subject =
      new ComputeVerifiedMobilePayment(
          mock(), mpbsRepositoryMock, mpbsServiceMock, feeServiceMock, paymentServiceMock);

  @Test
  void cannot_pay_already_paid_fee() {
    var mpbs = Mpbs.builder().id("mpbs1").fee(Fee.builder().remainingAmount(0).build()).build();
    var transaction =
        TransactionDetails.builder().pspTransactionAmount(200).status(SUCCESS).build();
    when(mpbsRepositoryMock.findByIdForUpdate(mpbs.getId())).thenReturn(Optional.of(mpbs));
    when(mpbsServiceMock.save(mpbs)).thenReturn(mpbs);

    assertThrows(NoRemainingAmountFee.class, () -> subject.saveTheVerifiedMpbs(mpbs, transaction));
  }

  @Test
  void skips_when_mpbs_already_paid_while_waiting_for_lock() {
    var mpbs = Mpbs.builder().id("mpbs2").fee(Fee.builder().remainingAmount(1000).build()).build();
    var transaction =
        TransactionDetails.builder().pspTransactionAmount(200).status(SUCCESS).build();
    when(mpbsRepositoryMock.findByIdForUpdate(mpbs.getId())).thenReturn(Optional.of(mpbs));
    when(paymentServiceMock.hasPaymentFromMpbs(mpbs.getId())).thenReturn(true);

    var result = subject.saveTheVerifiedMpbs(mpbs, transaction);

    assertNull(result);
    verifyNoInteractions(mpbsServiceMock);
  }

  @Test
  void reconciles_existing_manual_payment_instead_of_creating_another() {
    var fee = Fee.builder().id("fee1").remainingAmount(100_000).build();
    var mpbs = Mpbs.builder().id("mpbs3").fee(fee).build();
    var transaction =
        TransactionDetails.builder().pspTransactionAmount(300_000).status(SUCCESS).build();
    var manualPayment = Payment.builder().id("payment1").fee(fee).build();
    when(mpbsRepositoryMock.findByIdForUpdate(mpbs.getId())).thenReturn(Optional.of(mpbs));
    when(mpbsServiceMock.save(mpbs)).thenReturn(mpbs);
    when(paymentServiceMock.findUnreconciledPaymentByFeeId(fee.getId()))
        .thenReturn(Optional.of(manualPayment));

    var result = subject.saveTheVerifiedMpbs(mpbs, transaction);

    assertNotNull(result);
    assertEquals(100_000, fee.getRemainingAmount());
    verify(paymentServiceMock).reconcilePaymentWithMpbs(manualPayment, mpbs);
    verify(paymentServiceMock, never()).savePaymentFromMpbs(any(), anyInt());
  }
}
