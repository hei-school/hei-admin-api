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
import school.hei.haapi.model.exception.BadRequestException;
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
            getDestinationEmail(cor),
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
      throw new BadRequestException(
          "Interview date for the cor with id #%s is null".formatted(cor.getId()));
    }
    return LocalDateTime.ofInstant(interviewInstant, UTC3);
  }

  private static String toHourMinutes(LocalDateTime insertViewDate) {
    return "%dh%d".formatted(insertViewDate.getHour(), insertViewDate.getMinute());
  }

  private static InternetAddress getDestinationEmail(Cor cor) {
    var concernedStudent = cor.getStudent();
    if (concernedStudent == null) {
      throw new BadRequestException(
          "Concerned student for the cor with id #%s is null".formatted(cor.getId()));
    }

    return getEmailFromUserIdentifier(concernedStudent);
  }

  private static List<InternetAddress> getInterviewerEmails(Cor cor) {
    var interviewers = cor.getInterviewers();
    if (interviewers == null) {
      return List.of();
    }

    return interviewers.stream()
        .map(CorNotificationRequestedService::findEmailFromUserIdentifier)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();
  }

  private static Optional<InternetAddress> findEmailFromUserIdentifier(User user) {
    try {
      return Optional.of(getEmailFromUserIdentifier(user));
    } catch (BadRequestException e) {
      return Optional.empty();
    }
  }

  private static InternetAddress getEmailFromUserIdentifier(User user) {
    var email = user.getEmail();
    if (email == null) {
      throw new BadRequestException(
          "Email of the user with id #%s is null".formatted(user.getId()));
    }

    try {
      return new InternetAddress(email);
    } catch (AddressException e) {
      throw new BadRequestException("Email # " + email + "is not a valid address\n" + e);
    }
  }
}
