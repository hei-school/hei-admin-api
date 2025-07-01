package school.hei.haapi.unit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

import java.util.List;
import org.junit.jupiter.api.Test;
import school.hei.haapi.model.Mpbs.Mpbs;
import school.hei.haapi.service.FailedMobilePaymentNotification;

class FailedMobilePaymentNotificationTest {
  private final FailedMobilePaymentNotification subject =
      new FailedMobilePaymentNotification(mock());

  @Test
  void dont_throw_if_contain_null_values() {
    assertDoesNotThrow(() -> subject.accept(List.of(new Mpbs())));
  }
}
