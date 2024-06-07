package school.hei.haapi.service.event;

import static school.hei.haapi.model.User.Role.MANAGER;
import static school.hei.haapi.model.User.Role.TEACHER;
import static school.hei.haapi.model.User.Status.ENABLED;
import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;
import static school.hei.haapi.service.utils.TemplateUtils.htmlToString;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import school.hei.haapi.endpoint.event.gen.AnnouncementSendInit;
import school.hei.haapi.mail.Email;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.ApiException;
import school.hei.haapi.model.notEntity.Group;
import school.hei.haapi.service.UserService;

@Service
@AllArgsConstructor
@Slf4j
public class AnnouncementSendInitService implements Consumer<AnnouncementSendInit> {

  private final UserService userService;
  private final Mailer mailer;

  private List<User> getStudents(AnnouncementSendInit domain) {
    List<User> students = new ArrayList<>();
    for (Group group : domain.getGroups()) {
      List<User> byGroupId = userService.getByGroupId(group.getId());
      students.addAll(byGroupId);
    }
    return students;
  }

  private List<MailUser> getEmailUsers(AnnouncementSendInit domain) {
    var users =
        switch (domain.getScope()) {
          case GLOBAL -> userService.getAllEnabledUsers();
          case TEACHER -> userService.getByRoleAndStatus(TEACHER, ENABLED);
          case STUDENT -> getStudents(domain);
          case MANAGER -> userService.getByRoleAndStatus(MANAGER, ENABLED);
        };
    return users.stream().map(MailUser::of).toList();
  }

  public void sendEmail(AnnouncementSendInit domain) throws AddressException {
    String htmlBody = htmlToString("announcementEmail", getMailContext(domain));
    List<MailUser> users = getEmailUsers(domain);

    log.info("nb of email recipients = {}", users.size());
    List<InternetAddress> targetListAddress =
        users.stream().map(this::getInternetAddressFromUser).toList();

    mailer.accept(
        new Email(
            new InternetAddress("contact@mail.hei.school"),
            targetListAddress,
            List.of(),
            domain.getTitle(),
            htmlBody,
            List.of()));
  }

  private InternetAddress getInternetAddressFromUser(MailUser user) {
    try {
      return new InternetAddress(user.email());
    } catch (AddressException e) {
      log.info("bad email format {}", user.email);
      throw new ApiException(SERVER_EXCEPTION, e);
    }
  }

  private static Context getMailContext(AnnouncementSendInit announcement) {
    Context initial = new Context();
    initial.setVariable("fullName", announcement.getSenderFullName());
    initial.setVariable("id", announcement.getId());
    return initial;
  }

  @Override
  public void accept(AnnouncementSendInit announcementSendInit) {
    try {
      sendEmail(announcementSendInit);
      log.info("mailSent {} {}", announcementSendInit.getTitle(), announcementSendInit.getScope());
    } catch (AddressException e) {
      throw new RuntimeException(e);
    }
  }

  @Builder
  private record MailUser(String id, String email) {
    static MailUser of(User user) {
      return new MailUser(user.getId(), user.getEmail());
    }
  }
}
