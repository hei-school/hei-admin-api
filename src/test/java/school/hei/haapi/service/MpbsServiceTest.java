package school.hei.haapi.service;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.FAILED;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.PENDING;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.SUCCESS;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.haapi.model.Mpbs.Mpbs;
import school.hei.haapi.model.Mpbs.MpbsStatusHistory;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.repository.MpbsRepository;

class MpbsServiceTest {
  private final MpbsRepository mpbsRepository = mock();
  private final FeeService feeService = mock();
  private final MpbsService subject = new MpbsService(mpbsRepository, feeService);

  @BeforeEach
  void setUp() {
    when(mpbsRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
  }

  @Test
  void pend_failed_mpbs_decrease_to_2_remainingRetry_ok() {
    var now = Instant.now();
    List<MpbsStatusHistory> statusHistory =
        new ArrayList<>(
            asList(
                MpbsStatusHistory.builder()
                    .status(FAILED)
                    .creationInstant(now)
                    .updateInstant(now)
                    .build(),
                MpbsStatusHistory.builder()
                    .status(PENDING)
                    .creationInstant(now.minus(10, ChronoUnit.MINUTES))
                    .updateInstant(now.minus(10, ChronoUnit.MINUTES))
                    .build()));
    Mpbs mpbs = Mpbs.builder().id("mpbs").status(FAILED).statusHistory(statusHistory).build();
    when(mpbsRepository.findById(mpbs.getId())).thenReturn(Optional.of(mpbs.toBuilder().build()));

    Mpbs pended = subject.pendFailedMpbs(mpbs.getId());

    assertEquals(3, mpbs.getRemainingRetry());
    assertEquals(2, pended.getRemainingRetry());
  }

  @Test
  void pend_failed_mpbs_without_remainingRetry_ko() {
    var now = Instant.now();
    List<MpbsStatusHistory> statusHistory =
        new ArrayList<>(
            asList(
                MpbsStatusHistory.builder()
                    .status(FAILED)
                    .creationInstant(now)
                    .updateInstant(now)
                    .build(),
                MpbsStatusHistory.builder()
                    .status(PENDING)
                    .creationInstant(now.minus(10, ChronoUnit.MINUTES))
                    .updateInstant(now.minus(10, ChronoUnit.MINUTES))
                    .build()));
    Mpbs mpbs =
        Mpbs.builder()
            .id("mpbs")
            .status(FAILED)
            .statusHistory(statusHistory)
            .remainingRetry(0)
            .build();
    String mpbsId = mpbs.getId();
    when(mpbsRepository.findById(mpbsId)).thenReturn(Optional.of(mpbs.toBuilder().build()));

    var badRequestException =
        assertThrows(BadRequestException.class, () -> subject.pendFailedMpbs(mpbsId));
    assertEquals("Mpbs has no remaining retry #" + mpbsId, badRequestException.getMessage());
  }

  @Test
  void pend_not_failed_mpbs_ko() {
    var now = Instant.now();
    Mpbs mpbsSuccess =
        Mpbs.builder()
            .id("mpbsSuccess")
            .status(SUCCESS)
            .statusHistory(
                new ArrayList<>(
                    asList(
                        MpbsStatusHistory.builder()
                            .status(SUCCESS)
                            .creationInstant(now)
                            .updateInstant(now)
                            .build(),
                        MpbsStatusHistory.builder()
                            .status(PENDING)
                            .creationInstant(now.minus(10, ChronoUnit.MINUTES))
                            .updateInstant(now.minus(10, ChronoUnit.MINUTES))
                            .build())))
            .build();
    String mpbsSuccessId = mpbsSuccess.getId();
    when(mpbsRepository.findById(mpbsSuccessId))
        .thenReturn(Optional.of(mpbsSuccess.toBuilder().build()));
    Mpbs mpbsPending =
        Mpbs.builder()
            .id("mpbsPending")
            .status(PENDING)
            .statusHistory(
                new ArrayList<>(
                    singletonList(
                        MpbsStatusHistory.builder()
                            .status(PENDING)
                            .creationInstant(now.minus(10, ChronoUnit.MINUTES))
                            .updateInstant(now.minus(10, ChronoUnit.MINUTES))
                            .build())))
            .build();
    String mpbsPendingId = mpbsPending.getId();
    when(mpbsRepository.findById(mpbsPendingId))
        .thenReturn(Optional.of(mpbsPending.toBuilder().build()));

    var badRequestExceptionSuccess =
        assertThrows(BadRequestException.class, () -> subject.pendFailedMpbs(mpbsSuccessId));
    assertEquals("Mpbs must be fail #" + mpbsSuccessId, badRequestExceptionSuccess.getMessage());

    var badRequestExceptionPending =
        assertThrows(BadRequestException.class, () -> subject.pendFailedMpbs(mpbsSuccessId));
    assertEquals("Mpbs must be fail #" + mpbsSuccessId, badRequestExceptionPending.getMessage());
  }
}
