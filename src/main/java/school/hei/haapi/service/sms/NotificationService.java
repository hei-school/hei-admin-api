package school.hei.haapi.service.sms;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import school.hei.haapi.mail.Email;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.model.Notification;
import school.hei.haapi.model.NotificationResolutionStatus;
import school.hei.haapi.model.SmsCampaign;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.NotificationRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.dao.NotificationDao;

@Slf4j
@org.springframework.stereotype.Service
@AllArgsConstructor
public class NotificationService {
  private final NotificationRepository notificationRepository;
  private final NotificationDao notificationDao;
  private final UserRepository userRepository;
  private final Mailer mailer;

  public List<Notification> getByCriteria(
      String recipientId,
      Boolean read,
      NotificationResolutionStatus resolutionStatus,
      Pageable pageable) {
    return notificationDao.filterByCriteria(recipientId, read, resolutionStatus, pageable);
  }

  public Notification getById(String id) {
    return notificationRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Notification " + id + " not found"));
  }

  public Notification update(
      String id, Boolean read, NotificationResolutionStatus resolutionStatus) {
    var notification = getById(id);
    if (read != null) {
      notification.setRead(read);
    }
    if (resolutionStatus != null) {
      notification.setResolutionStatus(resolutionStatus);
    }
    return notificationRepository.save(notification);
  }

  public void notifyAdminsOfCampaignOutcome(SmsCampaign campaign, String subject, String htmlBody) {
    var admins = userRepository.findAllByRoleAndStatus(User.Role.ADMIN, User.Status.ENABLED);
    for (var admin : admins) {
      notificationRepository.save(
          Notification.builder()
              .id(UUID.randomUUID().toString())
              .recipient(admin)
              .subject(subject)
              .body(htmlBody)
              .resolutionStatus(NotificationResolutionStatus.UNRESOLVED)
              .smsCampaign(campaign)
              .build());
      sendEmail(admin, subject, htmlBody);
    }
  }

  private void sendEmail(User admin, String subject, String htmlBody) {
    try {
      mailer.accept(
          new Email(
              new InternetAddress(admin.getEmail()),
              List.of(),
              List.of(),
              subject,
              htmlBody,
              List.of()));
    } catch (AddressException e) {
      log.warn("Could not e-mail admin {}: invalid address {}", admin.getId(), admin.getEmail());
    }
  }
}
