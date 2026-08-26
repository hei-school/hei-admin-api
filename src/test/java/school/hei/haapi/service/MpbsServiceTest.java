package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.FAILED;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.PENDING;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.SUCCESS;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.rest.model.MpbsStatus;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.mpbs.Mpbs;
import school.hei.haapi.model.mpbs.MpbsStatusHistory;
import school.hei.haapi.repository.MpbsRepository;

class MpbsServiceTest extends FacadeITMockedThirdParties {
  @Autowired private MpbsService subject;
  @MockBean private MpbsRepository mpbsRepository;
  @MockBean private FeeService feeService;

  @BeforeEach
  void setUp() {
    when(mpbsRepository.saveAll(any())).thenAnswer(i -> i.getArguments()[0]);
  }

  @Test
  void save_unchanged_mpbs_not_add_history() {
    var mpbs = pendingMpbs(new ArrayList(List.of(pendingStatus())));

    var saved = subject.save(mpbs);

    assertEquals(mpbs, saved);
  }

  @Test
  void new_mpbs_add_history() {
    var mpbs = pendingMpbs(new ArrayList());

    var saved = subject.save(mpbs);

    assertEquals(1, saved.getStatusHistory().size());
    assertEquals(PENDING, saved.getStatusHistory().getFirst().getStatus());
  }

  @Test
  void add_history_on_status_change() {
    var mpbs = mpbs(new ArrayList(List.of(pendingStatus())), FAILED);

    var saved = subject.save(mpbs);

    assertEquals(2, saved.getStatusHistory().size());
    assertEquals(FAILED, saved.getStatusHistory().getLast().getStatus());
  }

  @Test
  void save_verified_successful_payment_applies_amount_when_still_pending() {
    var fee = Fee.builder().id("feeId").build();
    var verifiedMpbs =
        Mpbs.builder()
            .id("mpbs1")
            .amount(5000)
            .status(SUCCESS)
            .fee(fee)
            .statusHistory(new ArrayList<>(List.of(pendingStatus())))
            .build();
    when(mpbsRepository.findByIdForUpdate("mpbs1"))
        .thenReturn(Optional.of(Mpbs.builder().id("mpbs1").status(PENDING).build()));

    var result = subject.saveVerifiedSuccessfulPayment(verifiedMpbs);

    verify(feeService, times(1)).computeRemainingAmount("feeId", 5000);
    assertEquals(SUCCESS, result.getStatus());
  }

  @Test
  void save_verified_successful_payment_skips_when_already_resolved_under_the_lock() {
    var fee = Fee.builder().id("feeId").build();
    var verifiedMpbs = Mpbs.builder().id("mpbs1").amount(5000).status(SUCCESS).fee(fee).build();
    var alreadyResolved = Mpbs.builder().id("mpbs1").status(SUCCESS).build();
    when(mpbsRepository.findByIdForUpdate("mpbs1")).thenReturn(Optional.of(alreadyResolved));

    var result = subject.saveVerifiedSuccessfulPayment(verifiedMpbs);

    verify(feeService, never()).computeRemainingAmount(anyString(), anyInt());
    assertEquals(alreadyResolved, result);
  }

  private static Mpbs mpbs(List<MpbsStatusHistory> statusHistory, MpbsStatus status) {
    return Mpbs.builder()
        .status(status)
        .statusHistory(statusHistory)
        .creationDatetime(Instant.now())
        .build();
  }

  private static Mpbs pendingMpbs(List<MpbsStatusHistory> statusHistory) {
    return mpbs(statusHistory, PENDING);
  }

  private static MpbsStatusHistory pendingStatus() {
    return MpbsStatusHistory.builder().status(PENDING).creationInstant(Instant.now()).build();
  }
}
