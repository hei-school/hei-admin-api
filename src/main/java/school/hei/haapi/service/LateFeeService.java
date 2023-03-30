package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import school.hei.haapi.endpoint.event.EventConf;
import school.hei.haapi.endpoint.event.model.gen.LateFeeVerified;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.FeeRepository;
import school.hei.haapi.service.aws.SesService;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;

import static school.hei.haapi.service.utils.DataFormatterUtils.*;
import static school.hei.haapi.service.utils.TemplateUtils.htmlToString;

@Service
@AllArgsConstructor
public class LateFeeService implements Consumer<LateFeeVerified> {
  private final SesService sesService;
  private final EventConf eventConf;
  private final PaymentService paymentService;
  private final FeeRepository feeRepository;

  private static String emailSubject(User student, LateFeeVerified lateFee) {
    return "Retard de paiement - "
        + student.getRef()
        + " - "
        + lateFee.getComment();
  }

  private static String formatName(User student) {
    return student.getLastName()
        + " "
        + student.getFirstName();
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
    String sender = eventConf.getSesSource();
    String subject = emailSubject(student, lateFee);
    String htmlBody = htmlToString("lateFeeEmail", getMailContext(lateFee));
    sesService.sendEmail(sender, recipient, subject, htmlBody);
  }

  @Transactional
  public void updateLateFees(List<Fee> fees){
      Instant updateTime = Instant.now();
    for (Fee fee:fees ) {
      fee.setTotalAmount(paymentService.computeTotalAmount(fee));
      fee.setRemainingAmount(paymentService.computeRemainingAmountV2(fee));
      fee.setUpdatedAt(updateTime);
    }
    feeRepository.saveAll(fees);
  }

}
