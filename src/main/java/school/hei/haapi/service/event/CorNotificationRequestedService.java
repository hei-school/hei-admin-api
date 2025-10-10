package school.hei.haapi.service.event;

import static school.hei.haapi.service.utils.DataFormatterUtils.formatLocalDateTime;
import static school.hei.haapi.service.utils.InstantUtils.UTC3;
import static school.hei.haapi.service.utils.TemplateUtils.htmlToString;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import school.hei.haapi.endpoint.event.model.CorNotificationRequested;
import school.hei.haapi.mail.Email;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.model.Cor;
import school.hei.haapi.model.User;
import school.hei.haapi.service.CorService;

@Slf4j
@Service
@AllArgsConstructor
public class CorNotificationRequestedService implements Consumer<CorNotificationRequested> {
  private final Mailer mailer;
  private final CorService corService;

  @Override
  public void accept(CorNotificationRequested corNotificationRequested) {
    var cor = corService.getById(corNotificationRequested.getCorId());

    var htmlBody = htmlToString("corNotification", getMailContext(cor));
    mailer.accept(
        new Email(
            getEmailFromUser(cor.getStudent()),
            List.of(),
            getInterviewerEmails(cor),
            "[COR] Convocation des parents",
            htmlBody,
            List.of()));
  }

  private Context getMailContext(Cor cor) {
    var interviewDate = getInteviewLocalDateTime(cor);

    var context = new Context();
    context.setVariable("interviewers", cor.getInterviewers());
    context.setVariable("description", cor.getDescription());
    context.setVariable("date", formatLocalDateTime(interviewDate));
    context.setVariable("hour", toHourMinutes(interviewDate));
    return context;
  }

  private static LocalDateTime getInteviewLocalDateTime(Cor cor) {
    var interviewInstant = cor.getInterviewDatetime();
    if (interviewInstant == null) {
      throw new IllegalArgumentException(
          "Interview date for the cor with id #%s is null".formatted(cor.getId()));
    }
    return LocalDateTime.ofInstant(interviewInstant, UTC3);
  }

  private static String toHourMinutes(LocalDateTime insertViewDate) {
    return "%dh%d".formatted(insertViewDate.getHour(), insertViewDate.getMinute());
  }

  private static List<InternetAddress> getInterviewerEmails(Cor cor) {
    var interviewers = cor.getInterviewers();
    if (interviewers == null) {
      return List.of();
    }

    return interviewers.stream()
        .map(CorNotificationRequestedService::findEmailFromUser)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();
  }

  private static Optional<InternetAddress> findEmailFromUser(User user) {
    try {
      return Optional.of(getEmailFromUser(user));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  private static InternetAddress getEmailFromUser(User user) {
    var email = user.getEmail();

    try {
      return new InternetAddress(email);
    } catch (AddressException e) {
      throw new IllegalArgumentException(
          "Email # %s is not a valid address\n%s".formatted(email, e));
    }
  }
}
