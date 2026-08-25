package school.hei.haapi.service.event;

import static school.hei.haapi.model.User.Role.ADMIN;
import static school.hei.haapi.model.User.Status.ENABLED;
import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;
import static school.hei.haapi.service.utils.TemplateUtils.htmlToString;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import school.hei.haapi.endpoint.event.model.FeeArchiveRequested;
import school.hei.haapi.mail.Email;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.ApiException;
import school.hei.haapi.service.UserService;

@Service
@AllArgsConstructor
@Slf4j
public class FeeArchiveRequestedService implements Consumer<FeeArchiveRequested> {
  private final Mailer mailer;
  private final UserService userService;

  private static InternetAddress internetAddress(String email) {
    try {
      return new InternetAddress(email);
    } catch (AddressException e) {
      throw new ApiException(SERVER_EXCEPTION, e);
    }
  }

  private Context getMailContext(FeeArchiveRequested event) {
    var initial = new Context();
    initial.setVariable("studentRef", event.getStudentRef());
    initial.setVariable("studentFirstName", event.getStudentFirstName());
    initial.setVariable("studentLastName", event.getStudentLastName());
    initial.setVariable("totalAmount", event.getTotalAmount());
    initial.setVariable("dueDatetime", event.getDueDatetime());
    initial.setVariable("comment", event.getComment());
    return initial;
  }

  @Override
  public void accept(FeeArchiveRequested event) {
    var recipients =
        userService.getByRoleAndStatus(ADMIN, ENABLED).stream()
            .map(User::getEmail)
            .map(FeeArchiveRequestedService::internetAddress)
            .toList();
    if (recipients.isEmpty()) {
      log.warn(
          "No enabled admin found to notify about the archive request for fee {}",
          event.getFeeId());
      return;
    }

    var subject = "[ARCHIVAGE FRAIS EN ATTENTE] " + event.getStudentRef();
    var htmlBody = htmlToString("feeArchiveRequestedEmail", getMailContext(event));
    mailer.accept(
        new Email(
            recipients.getFirst(),
            recipients.subList(1, recipients.size()),
            List.of(),
            subject,
            htmlBody,
            List.of()));
    log.info("Archive request email for fee {} sent to {}", event.getFeeId(), recipients);
  }
}
