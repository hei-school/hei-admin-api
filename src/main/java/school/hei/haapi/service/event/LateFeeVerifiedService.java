package school.hei.haapi.service.event;

import static school.hei.haapi.service.utils.DataFormatterUtils.instantToCommonDate;
import static school.hei.haapi.service.utils.DataFormatterUtils.numberToReadable;
import static school.hei.haapi.service.utils.DataFormatterUtils.numberToWords;
import static school.hei.haapi.service.utils.TemplateUtils.htmlToString;

import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import school.hei.haapi.endpoint.email.SesConf;
import school.hei.haapi.endpoint.event.model.gen.LateFeeVerified;
import school.hei.haapi.model.User;
import school.hei.haapi.service.aws.SesService;

@Service
@AllArgsConstructor
public class LateFeeVerifiedService implements Consumer<LateFeeVerified> {
  private final SesService sesService;
  private final SesConf sesConf;

  private static String emailSubject(User student, LateFeeVerified lateFee) {
    return "Retard de paiement - " + student.getRef() + " - " + lateFee.getComment();
  }

  private static String formatName(User student) {
    return student.getLastName() + " " + student.getFirstName();
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
    User student = lateFee.getStudent();
    String recipient = student.getEmail();
    String sender = sesConf.getSesSource();
    String contact = sesConf.getSesContact();
    String subject = emailSubject(student, lateFee);
    String htmlBody = htmlToString("lateFeeEmail", getMailContext(lateFee));
    sesService.sendEmail(sender, contact, recipient, subject, htmlBody);
  }
}
