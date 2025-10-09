package school.hei.haapi.service.event;

import static school.hei.haapi.service.utils.DataFormatterUtils.instantToCommonDate;
import static school.hei.haapi.service.utils.InstantUtils.UTC3;
import static school.hei.haapi.service.utils.TemplateUtils.htmlToString;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import school.hei.haapi.endpoint.event.model.CorNotification;
import school.hei.haapi.mail.Email;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.model.exception.BadRequestException;

@Slf4j
@Service
@AllArgsConstructor
public class CorNotificationService implements Consumer<CorNotification> {
  private Mailer mailer;

  @Override
  public void accept(CorNotification corNotification) {
    var htmlBody = htmlToString("corNotification", getMailContext(corNotification));
    mailer.accept(
        new Email(
            getDestinationEmail(corNotification),
            List.of(),
            List.of(),
            "[COR] Convocation des parents",
            htmlBody,
            List.of()));
  }

  private Context getMailContext(CorNotification corNotification) {
    var interviewInstant = corNotification.getCor().getInterviewDate();
    if (interviewInstant == null) {
      throw new BadRequestException("Interview date is null");
    }
    var initial = new Context();
    initial.setVariable("interviewers", corNotification.getCor().getInterviewers());
    initial.setVariable("description", corNotification.getCor().getDescription());
    // TODO: Use the local datetime here
    initial.setVariable("date", instantToCommonDate(interviewInstant));
    var insertViewDate = LocalDateTime.ofInstant(interviewInstant, UTC3);
    initial.setVariable(
        "hour", "%dh%d".formatted(insertViewDate.getHour(), insertViewDate.getMinute()));
    return initial;
  }

  private static InternetAddress getDestinationEmail(CorNotification corNotification) {
    var concernedStudent = corNotification.getCor().getConcernedStudent();
    if (concernedStudent == null) {
      throw new BadRequestException("Concerned student is null");
    }

    var email = concernedStudent.getEmail();
    if (email == null) {
      throw new BadRequestException("Email is null");
    }

    try {
      return new InternetAddress(email);
    } catch (AddressException e) {
      throw new BadRequestException("Email # " + email + "is not a valid address\n" + e);
    }
  }
}
