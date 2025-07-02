package school.hei.haapi.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import school.hei.haapi.http.mapper.ExternalResponseMapper;
import school.hei.haapi.http.model.TransactionDetails;
import school.hei.haapi.model.MobileTransactionDetails;
import school.hei.haapi.model.Mpbs.Mpbs;
import school.hei.haapi.model.Mpbs.MpbsVerification;
import school.hei.haapi.service.ComputeVerifiedMobilePayement;
import school.hei.haapi.service.MobilePaymentService;
import school.hei.haapi.service.MobilePaymentUnverifiedHandler;
import school.hei.haapi.service.MpbsVerificationService;

class MpbsVerificationTest {
  MobilePaymentService mobilePaymentServiceMock = mock();
  MobilePaymentUnverifiedHandler mobilePaymentUnverifiedHandlerMock = mock();
  ComputeVerifiedMobilePayement computeVerifiedMobilePayementMock = mock();
  ExternalResponseMapper externalResponseMapper = new ExternalResponseMapper();
  MpbsVerificationService subject =
      new MpbsVerificationService(
          mock(),
          mock(),
          mobilePaymentServiceMock,
          externalResponseMapper,
          mock(),
          mock(),
          mobilePaymentUnverifiedHandlerMock,
          computeVerifiedMobilePayementMock);

  @Test
  void verification_split_verification_for_mbps() {
    var mbpsPending = Mpbs.builder().pspId("pending").fee(mock()).build();
    var mpbsVerified = Mpbs.builder().pspId("verified").fee(mock()).build();
    var correspondingMockTransactionsFromVerifiedMpbs =
        MobileTransactionDetails.builder()
            .pspTransactionRef(mpbsVerified.getPspId())
            .pspTransactionAmount(0)
            .build();
    MpbsVerification fakeComputedVerifiedMpbs = new MpbsVerification();
    when(mobilePaymentServiceMock.findAllTransactionByMpbsWithoutException(anyList()))
        .thenReturn(List.of(correspondingMockTransactionsFromVerifiedMpbs));
    TransactionDetails transactionsFromVerifiedMpbs =
        externalResponseMapper.toRestMobileTransactionDetails(
            correspondingMockTransactionsFromVerifiedMpbs);
    when(computeVerifiedMobilePayementMock.saveTheVerifiedMpbs(
            mpbsVerified, transactionsFromVerifiedMpbs))
        .thenReturn(fakeComputedVerifiedMpbs);

    List<MpbsVerification> verifiedMpbs =
        subject.verifyMobilePaymentAndSaveResult(List.of(mbpsPending, mpbsVerified));

    ArgumentCaptor<List<Mpbs>> argumentCaptor = ArgumentCaptor.forClass(List.class);
    verify(mobilePaymentUnverifiedHandlerMock, times(1)).accept(argumentCaptor.capture());
    List<Mpbs> mobilePaymentUnverified = argumentCaptor.getAllValues().getFirst();
    verify(computeVerifiedMobilePayementMock, never()).saveTheVerifiedMpbs(eq(mbpsPending), any());
    assertEquals(1, mobilePaymentUnverified.size());
    assertEquals(mbpsPending, mobilePaymentUnverified.getFirst());
    assertEquals(1, verifiedMpbs.size());
    assertEquals(fakeComputedVerifiedMpbs, verifiedMpbs.getFirst());
  }
}
