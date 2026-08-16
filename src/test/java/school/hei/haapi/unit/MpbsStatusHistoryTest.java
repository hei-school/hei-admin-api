package school.hei.haapi.unit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.FAILED;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.PENDING;

import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.model.MpbsStatus;
import school.hei.haapi.integration.conf.FakeDataProvider;
import school.hei.haapi.model.User;
import school.hei.haapi.model.mpbs.MpbsStatusHistory;

class MpbsStatusHistoryTest {
  FakeDataProvider fakeDataProvider = new FakeDataProvider();

  @Test
  void compare_with_have_same_status_ok() {
    var correctId = "id";
    var incorrectId = "bad id";
    var correctStatus = PENDING;
    var incorrectStatus = FAILED;
    var mpbsStatusHistory = someMpbsStatusHistory(correctId, correctStatus);
    var sameMpbsStatusHistory = someMpbsStatusHistory(correctId, correctStatus);
    var mpbsStatusHistoryWithIncorrectId = someMpbsStatusHistory(incorrectId, correctStatus);
    var mpbsStatusHistoryWithIncorrectStatus = someMpbsStatusHistory(correctId, incorrectStatus);

    assertFalse(mpbsStatusHistory.sameMpbsIdAndStatus(mpbsStatusHistoryWithIncorrectId));
    assertFalse(mpbsStatusHistory.sameMpbsIdAndStatus(mpbsStatusHistoryWithIncorrectStatus));
    assertTrue(mpbsStatusHistory.sameMpbsIdAndStatus(sameMpbsStatusHistory));
  }

  private MpbsStatusHistory someMpbsStatusHistory(String mpbsId, MpbsStatus status) {
    var mpbsInStatus = fakeDataProvider.someMpbs(new User());
    mpbsInStatus.setId(mpbsId);
    mpbsInStatus.setStatus(status);
    return MpbsStatusHistory.builder().mpbs(mpbsInStatus).status(status).build();
  }
}
