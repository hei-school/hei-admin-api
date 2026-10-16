package school.hei.haapi.unit.sms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.controller.SmsBalanceController;
import school.hei.haapi.service.befiana.BefianaBalanceResponse;
import school.hei.haapi.service.befiana.BefianaClient;

class SmsBalanceControllerTest {
  private final BefianaClient befianaClientMock = mock();
  private final SmsBalanceController subject = new SmsBalanceController(befianaClientMock);

  @Test
  void reads_the_balance_live_from_befiana_every_call() {
    var response = new BefianaBalanceResponse();
    response.setAvailable(320);
    when(befianaClientMock.getBalance()).thenReturn(response);

    var balance = subject.getSmsBalance();

    assertEquals(320, balance.getAvailableBalance());
  }

  @Test
  void a_null_available_from_befiana_is_treated_as_zero() {
    when(befianaClientMock.getBalance()).thenReturn(new BefianaBalanceResponse());

    var balance = subject.getSmsBalance();

    assertEquals(0, balance.getAvailableBalance());
  }
}
