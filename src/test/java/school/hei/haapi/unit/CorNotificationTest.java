package school.hei.haapi.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static school.hei.haapi.integration.conf.FakeDataProvider.someCor;
import static school.hei.haapi.integration.conf.FakeDataProvider.someStudent;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.event.model.CorNotification;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.model.Cor;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.service.CorService;
import school.hei.haapi.service.event.CorNotificationService;

class CorNotificationTest extends FacadeITMockedThirdParties {
  @Autowired private CorNotificationService subject;
  @MockBean private Mailer mailer;
  @MockBean private CorService corService;

  private User student;
  private User manager;
  private Cor cor;

  @BeforeEach
  void setUp() {
    student = someStudent("use with cor");
    manager = someStudent("cor manager");
    cor = someCor(student, Instant.now(), List.of(manager));

    when(corService.getById(cor.getId())).thenReturn(cor);
  }

  @Test
  void send_cor_mail_ok() {
    var notification = new CorNotification(cor.getId());

    subject.accept(notification);

    verify(mailer, times(1)).accept(any());
  }

  @Test
  void send_cor_mail_without_interview_date_ko() {
    cor.setInterviewDatetime(null);
    var notification = new CorNotification(cor.getId());

    var badRequestException =
        assertThrows(BadRequestException.class, () -> subject.accept(notification));
    assertEquals(
        "Interview date for the cor with id #%s is null".formatted(cor.getId()),
        badRequestException.getMessage());

    verify(mailer, never()).accept(any());
  }

  @Test
  void send_cor_mail_student_without_email_date_ko() {
    student.setEmail(null);
    var notification = new CorNotification(cor.getId());

    var badRequestException =
        assertThrows(BadRequestException.class, () -> subject.accept(notification));
    assertEquals(
        "Email of the user with id #%s is null".formatted(cor.getStudent().getId()),
        badRequestException.getMessage());

    verify(mailer, never()).accept(any());
  }
}
