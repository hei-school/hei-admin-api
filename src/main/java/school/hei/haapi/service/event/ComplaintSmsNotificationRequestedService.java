package school.hei.haapi.service.event;

import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.model.ComplaintSmsNotificationRequested;
import school.hei.haapi.model.ComplaintStatusHistory;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.ComplaintService;
import school.hei.haapi.service.befiana.BefianaClient;
import school.hei.haapi.service.befiana.BefianaException;

/**
 * Same derivation as {@link ComplaintEmailNotificationRequestedService}: recipient == the
 * complaint's student means the text is the latest status-history comment (that row always matches
 * the complaint's current status, whether IN_PROGRESS or just closed); any other recipient is
 * someone newly handling the complaint.
 */
@Slf4j
@Service
@AllArgsConstructor
public class ComplaintSmsNotificationRequestedService
    implements Consumer<ComplaintSmsNotificationRequested> {
  private final ComplaintService complaintService;
  private final UserRepository userRepository;
  private final BefianaClient befianaClient;

  @Override
  public void accept(ComplaintSmsNotificationRequested event) {
    var complaint = complaintService.getById(event.getComplaintId());
    var recipient =
        userRepository
            .findById(event.getUserId())
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "User with id #%s not found".formatted(event.getUserId())));

    var isStudent = recipient.getId().equals(complaint.getStudent().getId());
    var text =
        isStudent
            ? complaint
                .getLastStatusHistory()
                .map(ComplaintStatusHistory::getComment)
                .orElse("Votre réclamation a été mise à jour.")
            : "Une réclamation vous a été transmise sur HEI Admin.";

    var idempotencyKey =
        "%s-%s-%s".formatted(event.getComplaintId(), event.getUserId(), complaint.getStatus());

    try {
      befianaClient.sendSingle(recipient.getPhone(), text, idempotencyKey);
    } catch (BefianaException e) {
      log.warn(
          "BEFIANA SMS failed for complaint {} (recipient={}): {}",
          event.getComplaintId(),
          event.getUserId(),
          e.getMessage());
    }
  }
}
