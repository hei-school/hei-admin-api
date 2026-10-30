package school.hei.haapi.service.event;

import static java.time.format.DateTimeFormatter.ofLocalizedDateTime;
import static java.time.format.FormatStyle.LONG;
import static java.time.format.FormatStyle.SHORT;
import static java.util.Locale.FRENCH;
import static school.hei.haapi.model.ComplaintStatus.IN_PROGRESS;
import static school.hei.haapi.model.ComplaintStatus.REJECTED;
import static school.hei.haapi.service.utils.InstantUtils.UTC3;
import static school.hei.haapi.service.utils.TemplateUtils.htmlToString;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import school.hei.haapi.endpoint.event.model.ComplaintEmailNotificationRequested;
import school.hei.haapi.mail.Email;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.model.ComplaintAssignee;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.ComplaintAssigneeRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.ComplaintService;

@Slf4j
@Service
@AllArgsConstructor
public class ComplaintEmailNotificationRequestedService
    implements Consumer<ComplaintEmailNotificationRequested> {
  private final ComplaintService complaintService;
  private final ComplaintAssigneeRepository complaintAssigneeRepository;
  private final UserRepository userRepository;
  private final Mailer mailer;

  @Override
  public void accept(ComplaintEmailNotificationRequested event) {
    var complaint = complaintService.getById(event.getComplaintId());
    var recipient =
        userRepository
            .findById(event.getUserId())
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "User with id #%s not found".formatted(event.getUserId())));

    var context = new Context();
    context.setVariable("title", complaint.getTitle());
    context.setVariable("description", complaint.getDescription());
    context.setVariable("studentName", complaint.getStudent().getFullName());

    String template;
    String subject;
    if (recipient.getId().equals(complaint.getStudent().getId())) {
      if (event.isNotifyAssignment()) {
        var lastAssignee = findLastAssignee(event.getComplaintId());
        var assignedAt = lastAssignee.map(ComplaintAssignee::getAssignedAt).orElse(Instant.now());
        context.setVariable("date", formatDateFromInstant(assignedAt));
        lastAssignee.ifPresent(a -> context.setVariable("assigneeName", a.getUser().getFullName()));

        template = "complaintAssignedToStudentEmail";
        subject = "[Réclamation] Votre réclamation a été transmise à un responsable";
      } else {
        complaint
            .getLastStatusHistory()
            .ifPresent(
                h -> {
                  context.setVariable("message", h.getComment());
                  context.setVariable("date", formatDateFromInstant(h.getDatetime()));
                });

        if (complaint.getStatus() == IN_PROGRESS) {
          template = "complaintInProgressEmail";
          subject = "[Réclamation] Votre réclamation est en cours de traitement";
        } else {
          template = "complaintClosedEmail";
          subject =
              complaint.getStatus() == REJECTED
                  ? "[Réclamation] Votre réclamation a été rejetée"
                  : "[Réclamation] Votre réclamation a été résolue";
        }
      }
    } else {
      var assignee = findAssignee(event.getComplaintId(), recipient.getId());
      var wasAutoRouted = assignee.map(a -> a.getAssignedBy() == null).orElse(false);
      var assignedAt =
          assignee.map(ComplaintAssignee::getAssignedAt).orElse(complaint.getCreatedAt());

      template = "complaintAssignedEmail";
      subject =
          wasAutoRouted
              ? "[Réclamation] Nouvelle réclamation à traiter"
              : "[Réclamation] Une réclamation vous a été transmise";
      context.setVariable("wasAutoRouted", wasAutoRouted);
      context.setVariable("date", formatDateFromInstant(assignedAt));
    }

    var htmlBody = htmlToString(template, context);
    mailer.accept(
        new Email(
            toInternetAddress(recipient.getEmail()),
            List.of(),
            List.of(),
            subject,
            htmlBody,
            List.of()));
  }

  private Optional<ComplaintAssignee> findAssignee(String complaintId, String userId) {
    return complaintAssigneeRepository.findByComplaint_IdAndUser_Id(complaintId, userId);
  }

  private Optional<ComplaintAssignee> findLastAssignee(String complaintId) {
    return complaintAssigneeRepository.findTopByComplaint_IdOrderByAssignedAtDesc(complaintId);
  }

  private static String formatDateFromInstant(Instant instant) {
    return instant
        .atZone(UTC3)
        .toLocalDateTime()
        .format(ofLocalizedDateTime(LONG, SHORT).withLocale(FRENCH));
  }

  private static InternetAddress toInternetAddress(String email) {
    try {
      return new InternetAddress(email);
    } catch (AddressException e) {
      throw new IllegalArgumentException("Email %s is not a valid address".formatted(email));
    }
  }
}
