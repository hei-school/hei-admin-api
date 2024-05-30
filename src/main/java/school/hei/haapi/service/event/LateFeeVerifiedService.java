package school.hei.haapi.service.event;

import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;
import static school.hei.haapi.service.utils.DataFormatterUtils.instantToCommonDate;
import static school.hei.haapi.service.utils.DataFormatterUtils.numberToReadable;
import static school.hei.haapi.service.utils.DataFormatterUtils.numberToWords;
import static school.hei.haapi.service.utils.TemplateUtils.htmlToString;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import school.hei.haapi.endpoint.event.gen.LateFeeVerified;
import school.hei.haapi.mail.Email;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.model.exception.ApiException;

@Service
@AllArgsConstructor
public class LateFeeVerifiedService implements Consumer<LateFeeVerified> {
  private final Mailer mailer;

  private static String emailSubject(LateFeeVerified.FeeUser student, LateFeeVerified lateFee) {
    return "Retard de paiement - " + student.ref() + " - " + lateFee.getComment();
  }

  private static String formatName(LateFeeVerified.FeeUser student) {
    return student.lastName() + " " + student.firstName();
  }

  private static Context getMailContext(LateFeeVerified lateFee) {
    Context initial = new Context();
    initial.setVariable("fullName", formatName(lateFee.getStudent()));
    initial.setVariable("comment", lateFee.getComment());
    initial.setVariable("dueDatetime", instantToCommonDate(lateFee.getDueDatetime()));
    initial.setVariable("remainingAmount", numberToReadable(lateFee.getRemainingAmount()));
    initial.setVariable("remainingAmWords", numberToWords(lateFee.getRemainingAmount()));
    return initial;
  }

  @Override
  public void accept(LateFeeVerified lateFee) {
    LateFeeVerified.FeeUser student = lateFee.getStudent();
    String subject = emailSubject(student, lateFee);
    String htmlBody = htmlToString("lateFeeEmail", getMailContext(lateFee));
    try {
      mailer.accept(
          new Email(
              new InternetAddress(student.email()),
              List.of(),
              List.of(),
              subject,
              htmlBody,
              List.of()));
    } catch (AddressException e) {
      throw new ApiException(SERVER_EXCEPTION, e);
    }
  }
}
