package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.FAILED;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.PENDING;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.rest.model.MpbsStatus;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
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
