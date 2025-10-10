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
import school.hei.haapi.endpoint.event.model.CorNotification;
import school.hei.haapi.endpoint.rest.model.UserIdentifier;
import school.hei.haapi.mail.Email;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.model.exception.BadRequestException;

@Slf4j
@Service
@AllArgsConstructor
public class CorNotificationService implements Consumer<CorNotification> {
  private final Mailer mailer;

  @Override
  public void accept(CorNotification corNotification) {
    var htmlBody = htmlToString("corNotification", getMailContext(corNotification));
    mailer.accept(
        new Email(
            getDestinationEmail(corNotification),
            List.of(),
            getInterviewerEmails(corNotification),
            "[COR] Convocation des parents",
            htmlBody,
            List.of()));
  }

  private Context getMailContext(CorNotification corNotification) {
    var interviewDate = getInteviewLocalDateTime(corNotification);

    var context = new Context();
    context.setVariable("interviewers", corNotification.getCor().getInterviewers());
    context.setVariable("description", corNotification.getCor().getDescription());
    context.setVariable("date", formatLocalDateTime(interviewDate));
    context.setVariable("hour", toHourMinutes(interviewDate));
    return context;
  }

  private static LocalDateTime getInteviewLocalDateTime(CorNotification corNotification) {
    var interviewInstant = corNotification.getCor().getInterviewDate();
    if (interviewInstant == null) {
      throw new BadRequestException(
          "Interview date for the cor with id #%s is null"
              .formatted(corNotification.getCor().getId()));
    }
    return LocalDateTime.ofInstant(interviewInstant, UTC3);
  }

  private static String toHourMinutes(LocalDateTime insertViewDate) {
    return "%dh%d".formatted(insertViewDate.getHour(), insertViewDate.getMinute());
  }

  private static InternetAddress getDestinationEmail(CorNotification corNotification) {
    var concernedStudent = corNotification.getCor().getConcernedStudent();
    if (concernedStudent == null) {
      throw new BadRequestException(
          "Concerned student for the cor with id #%s is null"
              .formatted(corNotification.getCor().getId()));
    }

    return getEmailFromUserIdentifier(concernedStudent);
  }

  private static List<InternetAddress> getInterviewerEmails(CorNotification corNotification) {
    var interviewers = corNotification.getCor().getInterviewers();
    if (interviewers == null) {
      return List.of();
    }

    return interviewers.stream()
        .map(CorNotificationService::findEmailFromUserIdentifier)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();
  }

  private static Optional<InternetAddress> findEmailFromUserIdentifier(UserIdentifier user) {
    try {
      return Optional.of(getEmailFromUserIdentifier(user));
    } catch (BadRequestException e) {
      return Optional.empty();
    }
  }

  private static InternetAddress getEmailFromUserIdentifier(UserIdentifier user) {
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
