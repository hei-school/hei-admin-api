package school.hei.haapi.unit.sms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.model.SmsCampaign;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.NotificationRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.dao.NotificationDao;
import school.hei.haapi.service.sms.NotificationService;

class NotificationServiceTest {
  private final NotificationRepository notificationRepositoryMock = mock();
  private final NotificationDao notificationDaoMock = mock();
  private final UserRepository userRepositoryMock = mock();
  private final Mailer mailerMock = mock();

  private final NotificationService subject =
      new NotificationService(
          notificationRepositoryMock, notificationDaoMock, userRepositoryMock, mailerMock);

  @Test
  void notifies_every_enabled_admin_with_one_row_and_one_email_each() {
    var admin1 =
        User.builder().id("admin1").email("admin1@hei.school").role(User.Role.ADMIN).build();
    var admin2 =
        User.builder().id("admin2").email("admin2@hei.school").role(User.Role.ADMIN).build();
    when(userRepositoryMock.findAllByRoleAndStatus(User.Role.ADMIN, User.Status.ENABLED))
        .thenReturn(List.of(admin1, admin2));
    var campaign = SmsCampaign.builder().id("campaign1").build();

    subject.notifyAdminsOfCampaignOutcome(campaign, "Subject", "<p>Body</p>");

    verify(notificationRepositoryMock, times(2)).save(any());
    verify(mailerMock, times(2)).accept(any());
  }

  @Test
  void an_admin_with_an_invalid_email_still_gets_their_notification_row() {
    var admin = User.builder().id("admin1").email("Admin <invalid").role(User.Role.ADMIN).build();
    when(userRepositoryMock.findAllByRoleAndStatus(User.Role.ADMIN, User.Status.ENABLED))
        .thenReturn(List.of(admin));
    var campaign = SmsCampaign.builder().id("campaign1").build();

    subject.notifyAdminsOfCampaignOutcome(campaign, "Subject", "<p>Body</p>");

    verify(notificationRepositoryMock, times(1)).save(any());
    verify(mailerMock, never()).accept(any());
  }

  @Test
  void no_enabled_admin_means_no_notification_and_no_email() {
    when(userRepositoryMock.findAllByRoleAndStatus(User.Role.ADMIN, User.Status.ENABLED))
        .thenReturn(List.of());
    var campaign = SmsCampaign.builder().id("campaign1").build();

    subject.notifyAdminsOfCampaignOutcome(campaign, "Subject", "<p>Body</p>");

    verify(notificationRepositoryMock, never()).save(any());
    verify(mailerMock, never()).accept(any());
  }

  @Test
  void update_only_touches_the_fields_actually_provided() {
    var notification =
        school.hei.haapi.model.Notification.builder()
            .id("n1")
            .read(false)
            .resolutionStatus(school.hei.haapi.model.NotificationResolutionStatus.UNRESOLVED)
            .build();
    when(notificationRepositoryMock.findById("n1")).thenReturn(java.util.Optional.of(notification));
    when(notificationRepositoryMock.save(any()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var updated = subject.update("n1", true, null);

    assertEquals(true, updated.isRead());
    assertEquals(
        school.hei.haapi.model.NotificationResolutionStatus.UNRESOLVED,
        updated.getResolutionStatus());
  }
}
