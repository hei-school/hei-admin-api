package school.hei.haapi.unit.sms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.controller.SmsBalanceController;
import school.hei.haapi.service.befiana.BefianaClient;

class SmsBalanceControllerTest {
  private final BefianaClient befianaClientMock = mock();
  private final SmsBalanceController subject = new SmsBalanceController(befianaClientMock);

  @Test
  void reads_the_balance_live_from_befiana_every_call() {
    when(befianaClientMock.getBalance()).thenReturn(320);

    var balance = subject.getSmsBalance();

    assertEquals(320, balance.getAvailableBalance());
  }
}
