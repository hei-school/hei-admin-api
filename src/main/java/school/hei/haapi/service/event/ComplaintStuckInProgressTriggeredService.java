package school.hei.haapi.service.event;

import static school.hei.haapi.service.utils.TemplateUtils.htmlToString;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import school.hei.haapi.endpoint.event.model.ComplaintStuckInProgressTriggered;
import school.hei.haapi.mail.Email;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.model.Complaint;
import school.hei.haapi.model.ComplaintStatusHistory;
import school.hei.haapi.repository.ComplaintRepository;
import school.hei.haapi.repository.ComplaintStatusHistoryRepository;
import school.hei.haapi.service.befiana.BefianaClient;
import school.hei.haapi.service.befiana.BefianaException;

@Slf4j
@Service
@AllArgsConstructor
public class ComplaintStuckInProgressTriggeredService
    implements Consumer<ComplaintStuckInProgressTriggered> {
  private static final Duration STUCK_THRESHOLD = Duration.ofDays(3);

  private final ComplaintStatusHistoryRepository complaintStatusHistoryRepository;
  private final ComplaintRepository complaintRepository;
  private final Mailer mailer;
  private final BefianaClient befianaClient;

  @Override
  public void accept(ComplaintStuckInProgressTriggered event) {
    var threshold = Instant.now().minus(STUCK_THRESHOLD);
    var stuckHistories = complaintStatusHistoryRepository.findStuckInProgressSince(threshold);

    stuckHistories.stream().map(ComplaintStatusHistory::getComplaint).forEach(this::remind);
  }

  private void remind(Complaint complaint) {
    var student = complaint.getStudent();

    try {
      befianaClient.sendSingle(
          student.getPhone(),
          "Votre réclamation est toujours en cours de traitement.",
          complaint.getId() + "-reminder");
    } catch (BefianaException e) {
      log.warn("BEFIANA reminder failed for complaint {}: {}", complaint.getId(), e.getMessage());
    }

    var context = new Context();
    context.setVariable("title", complaint.getTitle());
    var htmlBody = htmlToString("complaintStuckInProgressEmail", context);
    mailer.accept(
        new Email(
            toInternetAddress(student.getEmail()),
            List.of(),
            List.of(),
            "[Réclamation] Toujours en cours de traitement",
            htmlBody,
            List.of()));

    complaint.setReminderSentAt(Instant.now());
    complaintRepository.save(complaint);
  }

  private static InternetAddress toInternetAddress(String email) {
    try {
      return new InternetAddress(email);
    } catch (AddressException e) {
      throw new IllegalArgumentException("Email %s is not a valid address".formatted(email));
    }
  }
}
