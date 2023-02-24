package school.hei.haapi.service;

import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import school.hei.haapi.endpoint.event.EventConf;
import school.hei.haapi.endpoint.event.model.gen.LateFeeChecked;
import school.hei.haapi.model.User;
import school.hei.haapi.service.aws.SesService;

import static school.hei.haapi.service.utils.TemplateUtils.htmlToString;
import static school.hei.haapi.service.utils.TypeFormatUtils.formatInstant;
import static school.hei.haapi.service.utils.TypeFormatUtils.formatNumber;
import static school.hei.haapi.service.utils.TypeFormatUtils.formatNumberToWords;

@Service
@AllArgsConstructor
public class LateFeeCheckedService implements Consumer<LateFeeChecked> {
  private final SesService service;
  private final EventConf eventConf;

  @Override
  public void accept(LateFeeChecked lateFeeChecked) {
    String sender = eventConf.getSesSource();
    User student = lateFeeChecked.getStudent();
    String recipient = student.getEmail();
    String subject = "Retard de paiement - " +
        student.getRef() +
        " - " +
        lateFeeChecked.getComment();
    StringBuilder fullName = new StringBuilder()
        .append(student.getLastName())
        .append(" ")
        .append(student.getFirstName());
    String formattedDate = formatInstant(lateFeeChecked.getDueDatetime());
    String formattedNumber = formatNumber(lateFeeChecked.getRemainingAmount());
    String formattedToWords = formatNumberToWords(lateFeeChecked.getRemainingAmount());
    Context context = new Context();
    context.setVariable("fullName", fullName);
    context.setVariable("comment", lateFeeChecked.getComment());
    context.setVariable("dueDatetime", formattedDate);
    context.setVariable("remainingAmount", formattedNumber);
    context.setVariable("remainingAmWords", formattedToWords);
    String bodyHtml = htmlToString("lateFee", context);
    service.sendEmail(sender, recipient, subject, bodyHtml);
  }
}
