package school.hei.haapi.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static school.hei.haapi.integration.conf.FakeDataProvider.someCor;
import static school.hei.haapi.integration.conf.FakeDataProvider.someStudent;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.event.model.CorNotification;
import school.hei.haapi.endpoint.rest.mapper.CorMapper;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.service.event.CorNotificationService;

public class CorNotificationTest extends FacadeITMockedThirdParties {
  @Autowired private CorNotificationService subject;
  @Autowired private CorMapper corMapper;
  @MockBean private Mailer mailer;

  @Test
  void send_cor_mail_ok() {
    var student = someStudent("use with cor");
    var manager = someStudent("cor manager");
    var cor = someCor(student, Instant.now(), List.of(manager));
    var notification = new CorNotification(corMapper.toRest(cor));
    subject.accept(notification);

    verify(mailer, times(1)).accept(any());
  }
}
