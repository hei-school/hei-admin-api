package school.hei.haapi.service;

import static java.time.temporal.ChronoUnit.MINUTES;
import static java.util.Arrays.stream;
import static java.util.Collections.reverse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
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
import school.hei.haapi.endpoint.rest.model.MpbsStatus;
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
    Mpbs mpbs =
        Mpbs.builder()
            .id("mpbs")
            .status(FAILED)
            .statusHistory(generateHistory(PENDING, FAILED))
            .build();
    when(mpbsRepository.findById(mpbs.getId())).thenReturn(Optional.of(mpbs.toBuilder().build()));

    Mpbs pended = subject.pendFailedMpbs(mpbs.getId());

    assertEquals(3, mpbs.getRemainingRetry());
    assertEquals(2, pended.getRemainingRetry());
  }

  @Test
  void pend_failed_mpbs_without_remainingRetry_ko() {
    Mpbs mpbs =
        Mpbs.builder()
            .id("mpbs")
            .status(FAILED)
            .statusHistory(generateHistory(PENDING, FAILED))
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
    Mpbs mpbsSuccess =
        Mpbs.builder()
            .id("mpbsSuccess")
            .status(SUCCESS)
            .statusHistory(generateHistory(PENDING, SUCCESS))
            .build();
    String mpbsSuccessId = mpbsSuccess.getId();
    when(mpbsRepository.findById(mpbsSuccessId))
        .thenReturn(Optional.of(mpbsSuccess.toBuilder().build()));
    Mpbs mpbsPending =
        Mpbs.builder()
            .id("mpbsPending")
            .status(PENDING)
            .statusHistory(generateHistory(PENDING))
            .build();
    String mpbsPendingId = mpbsPending.getId();
    when(mpbsRepository.findById(mpbsPendingId))
        .thenReturn(Optional.of(mpbsPending.toBuilder().build()));

    var badRequestExceptionSuccess =
        assertThrows(BadRequestException.class, () -> subject.pendFailedMpbs(mpbsSuccessId));
    assertEquals("Mpbs must be failing #" + mpbsSuccessId, badRequestExceptionSuccess.getMessage());

    var badRequestExceptionPending =
        assertThrows(BadRequestException.class, () -> subject.pendFailedMpbs(mpbsSuccessId));
    assertEquals("Mpbs must be failing #" + mpbsSuccessId, badRequestExceptionPending.getMessage());
  }

  private List<MpbsStatusHistory> generateHistory(MpbsStatus... statuses) {
    var now = Instant.now();
    var history = new ArrayList<MpbsStatusHistory>();
    List<MpbsStatus> reversedStatuses = new ArrayList<>(stream(statuses).toList());
    reverse(reversedStatuses);
    for (MpbsStatus status : reversedStatuses) {
      history.add(
          MpbsStatusHistory.builder()
              .status(status)
              .creationInstant(now)
              .updateInstant(now)
              .build());
      now = now.minus(10, MINUTES);
    }
    return history;
  }
}
