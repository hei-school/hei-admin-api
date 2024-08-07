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
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import school.hei.haapi.endpoint.event.model.LateFeeVerified;
import school.hei.haapi.mail.Email;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.model.exception.ApiException;
import school.hei.haapi.service.UserService;
import school.hei.haapi.service.utils.Base64Converter;
import school.hei.haapi.service.utils.ClassPathResourceResolver;

@Service
@AllArgsConstructor
@Slf4j
public class LateFeeVerifiedService implements Consumer<LateFeeVerified> {
  private final Mailer mailer;
  private final UserService userService;
  private final Base64Converter base64Converter;
  private final ClassPathResourceResolver classPathResourceResolver;

  private static String emailSubject(LateFeeVerified.FeeUser student, LateFeeVerified lateFee) {
    return "Retard de paiement - " + student.ref() + " - " + lateFee.getComment();
  }

  private static String formatName(LateFeeVerified.FeeUser student) {
    return student.lastName() + " " + student.firstName();
  }

  private Context getMailContext(LateFeeVerified lateFee) {
    Context initial = new Context();
    Resource emailSignatureImage = classPathResourceResolver.apply("Signature-HEI-v2", ".png");

    initial.setVariable("fullName", formatName(lateFee.getStudent()));
    initial.setVariable("comment", lateFee.getComment());
    initial.setVariable("dueDatetime", instantToCommonDate(lateFee.getDueDatetime()));
    initial.setVariable("remainingAmount", numberToReadable(lateFee.getRemainingAmount()));
    initial.setVariable("remainingAmWords", numberToWords(lateFee.getRemainingAmount()));
    initial.setVariable("emailSignature", base64Converter.apply(emailSignatureImage));
    return initial;
  }

  @Override
  public void accept(LateFeeVerified lateFee) {
    // Suspend student who has late fees ...
    LateFeeVerified.FeeUser concernedUser = lateFee.getStudent();
    userService.suspendStudentById(concernedUser.id());
    log.info("Student with ref." + concernedUser.ref() + " has been flagged to SUSPENDED");

    // ... Then send mail
    String subject = emailSubject(concernedUser, lateFee);
    String htmlBody = htmlToString("lateFeeEmail", getMailContext(lateFee));
    try {
      mailer.accept(
          new Email(
              new InternetAddress(concernedUser.email()),
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
