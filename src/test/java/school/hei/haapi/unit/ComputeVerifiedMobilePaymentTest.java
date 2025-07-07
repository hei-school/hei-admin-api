package school.hei.haapi.unit;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import school.hei.haapi.http.model.TransactionDetails;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.Mpbs.Mpbs;
import school.hei.haapi.model.exception.NoRemainingAmountFee;
import school.hei.haapi.service.ComputeVerifiedMobilePayment;
import school.hei.haapi.service.FeeService;
import school.hei.haapi.service.MpbsService;

class ComputeVerifiedMobilePaymentTest {
  private final MpbsService mpbsServiceMock = mock();
  private final FeeService feeServiceMock =
      new FeeService(mock(), mock(), mock(), mock(), mock(), mock(), mock());
  private final ComputeVerifiedMobilePayment subject =
      new ComputeVerifiedMobilePayment(mock(), mpbsServiceMock, feeServiceMock, mock());

  @Test
  void cannot_pay_already_paid_fee() {
    Mpbs mpbs = Mpbs.builder().fee(Fee.builder().remainingAmount(0).build()).build();
    TransactionDetails transaction = TransactionDetails.builder().pspTransactionAmount(200).build();
    when(mpbsServiceMock.save(mpbs)).thenReturn(mpbs);

    assertThrows(NoRemainingAmountFee.class, () -> subject.saveTheVerifiedMpbs(mpbs, transaction));
  }
}
