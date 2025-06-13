package school.hei.haapi.endpoint.event;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static school.hei.haapi.integration.conf.TestUtils.FEE1_ID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.event.model.PaidFeeByMpbsNotificationBody;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.model.exception.ApiException;
import school.hei.haapi.service.event.PaidFeeByMpbsNotificationBodyService;

class MailingTest extends FacadeITMockedThirdParties {
  @Autowired PaidFeeByMpbsNotificationBodyService paidFeeByMpbsNotificationBodyService;
  @MockBean Mailer mailer;

  @Test
  void paid_fee_by_mpbs_notification_send_mail() {
    paidFeeByMpbsNotificationBodyService.accept(
        new PaidFeeByMpbsNotificationBody("", "email@gmail.com", 0, FEE1_ID));

    verify(mailer, times(1)).accept(any());
  }

  @Test
  void paid_fee_by_mpbs_notification_not_send_for_bad_mail() {
    var paidFeeByMpbsNotificationBody = new PaidFeeByMpbsNotificationBody("", "", 0, FEE1_ID);
    assertThrows(
        ApiException.class,
        () -> paidFeeByMpbsNotificationBodyService.accept(paidFeeByMpbsNotificationBody));

    verify(mailer, times(0)).accept(any());
  }
}
