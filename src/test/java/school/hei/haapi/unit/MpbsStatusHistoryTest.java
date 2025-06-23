package school.hei.haapi.unit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.PENDING;

import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.model.MpbsStatus;
import school.hei.haapi.integration.conf.FakeDataProvider;
import school.hei.haapi.model.Mpbs.Mpbs;
import school.hei.haapi.model.Mpbs.MpbsStatusHistory;
import school.hei.haapi.model.User;

class MpbsStatusHistoryTest {
  FakeDataProvider fakeDataProvider = new FakeDataProvider();

  @Test
  void compare_with_have_same_status_ok() {
    var mpbsStatusHistory1 = someMpbsStatusHistory("id", PENDING);
    var mpbsStatusHistory2 =
        someMpbsStatusHistory(mpbsStatusHistory1.getMpbs().getId(), mpbsStatusHistory1.getStatus());

    assertTrue(mpbsStatusHistory1.sameMpbsIdAndStatus(mpbsStatusHistory2));
  }

  private MpbsStatusHistory someMpbsStatusHistory(String id, MpbsStatus status) {
    Mpbs mpbsInStatus = fakeDataProvider.someMpbs(new User());
    mpbsInStatus.setId(id);
    mpbsInStatus.setStatus(status);
    return MpbsStatusHistory.builder().mpbs(mpbsInStatus).status(status).build();
  }
}
