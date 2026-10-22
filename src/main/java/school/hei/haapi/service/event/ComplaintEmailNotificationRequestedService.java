package school.hei.haapi.service.event;

import static school.hei.haapi.model.ComplaintStatus.REJECTED;
import static school.hei.haapi.service.utils.TemplateUtils.htmlToString;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import school.hei.haapi.endpoint.event.model.ComplaintEmailNotificationRequested;
import school.hei.haapi.mail.Email;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.ComplaintAssigneeRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.ComplaintService;

/**
 * One event, two possible notifications, told apart by comparing the recipient to the complaint's
 * own state rather than by a redundant "kind" field: the student is only ever notified by email
 * when the complaint closes, so recipient == student means "closed"; any other recipient is someone
 * handling the complaint (the auto-routed manager or a transmitted assignee — told apart via that
 * ComplaintAssignee row's assignedBy).
 */
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
      template = "complaintClosedEmail";
      subject =
          complaint.getStatus() == REJECTED
              ? "[Réclamation] Votre réclamation a été rejetée"
              : "[Réclamation] Votre réclamation a été résolue";
      complaint
          .getLastStatusHistory()
          .ifPresent(h -> context.setVariable("message", h.getComment()));
    } else {
      var wasAutoRouted = wasAutoRouted(event.getComplaintId(), recipient.getId());
      template = "complaintAssignedEmail";
      subject =
          wasAutoRouted
              ? "[Réclamation] Nouvelle réclamation à traiter"
              : "[Réclamation] Une réclamation vous a été transmise";
      context.setVariable("wasAutoRouted", wasAutoRouted);
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

  private boolean wasAutoRouted(String complaintId, String userId) {
    return complaintAssigneeRepository
        .findByComplaint_IdAndUser_Id(complaintId, userId)
        .map(assignee -> assignee.getAssignedBy() == null)
        .orElse(false);
  }

  private static InternetAddress toInternetAddress(String email) {
    try {
      return new InternetAddress(email);
    } catch (AddressException e) {
      throw new IllegalArgumentException("Email %s is not a valid address".formatted(email));
    }
  }
}
