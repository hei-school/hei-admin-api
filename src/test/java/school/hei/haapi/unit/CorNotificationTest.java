package school.hei.haapi.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.service.event.CorNotificationService;

class CorNotificationTest extends FacadeITMockedThirdParties {
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

  @Test
  void send_cor_mail_without_interview_date_ko() {
    var student = someStudent("use with cor");
    var manager = someStudent("cor manager");
    var cor = someCor(student, Instant.now(), List.of(manager));
    cor.setInterviewDatetime(null);
    var notification = new CorNotification(corMapper.toRest(cor));

    var badRequestException =
        assertThrows(BadRequestException.class, () -> subject.accept(notification));
    assertEquals(
        "Interview date for the cor with id #%s is null".formatted(cor.getId()),
        badRequestException.getMessage());

    verify(mailer, never()).accept(any());
  }

  @Test
  void send_cor_mail_student_without_email_date_ko() {
    var student = someStudent("use with cor");
    student.setEmail(null);
    var manager = someStudent("cor manager");
    var cor = someCor(student, Instant.now(), List.of(manager));
    var notification = new CorNotification(corMapper.toRest(cor));

    var badRequestException =
        assertThrows(BadRequestException.class, () -> subject.accept(notification));
    assertEquals(
        "Email of the user with id #%s is null".formatted(cor.getStudent().getId()),
        badRequestException.getMessage());

    verify(mailer, never()).accept(any());
  }
}
