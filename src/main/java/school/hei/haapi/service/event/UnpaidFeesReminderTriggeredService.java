package school.hei.haapi.service.event;

import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;
import static school.hei.haapi.service.utils.TemplateUtils.htmlToString;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import school.hei.haapi.endpoint.event.gen.UnpaidFeesReminder;
import school.hei.haapi.mail.Email;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.model.exception.ApiException;

@Service
@AllArgsConstructor
public class UnpaidFeesReminderTriggeredService implements Consumer<UnpaidFeesReminder> {
  private Mailer mailer;

  private static Context getMailContext(UnpaidFeesReminder unpaidFee) {
    Context initial = new Context();
    initial.setVariable("comment", unpaidFee.getComment());
    initial.setVariable("dueDatetime", unpaidFee.getDueDatetime());
    initial.setVariable("remainingAmount", unpaidFee.getRemainingAmount());
    return initial;
  }

  @Override
  public void accept(UnpaidFeesReminder unpaidFeesReminder) {
    String htmlBody = htmlToString("unpaidFeeReminderEmail", getMailContext(unpaidFeesReminder));
    try {
      mailer.accept(
          new Email(
              new InternetAddress(unpaidFeesReminder.getStudentEmail()),
              List.of(),
              List.of(),
              "Rappel - Paiement de mensualité",
              htmlBody,
              List.of()));
    } catch (AddressException e) {
      throw new ApiException(SERVER_EXCEPTION, e);
    }
  }
}
