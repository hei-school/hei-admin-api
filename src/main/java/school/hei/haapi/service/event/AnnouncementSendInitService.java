package school.hei.haapi.service.event;

import static school.hei.haapi.model.User.Role.MANAGER;
import static school.hei.haapi.model.User.Role.TEACHER;
import static school.hei.haapi.model.User.Status.ENABLED;
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
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import school.hei.haapi.endpoint.event.gen.AnnouncementSendInit;
import school.hei.haapi.mail.Email;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.model.User;
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
    log.info("groups = {}", domain.getGroups());
    for (Group group : domain.getGroups()) {
      List<User> byGroupId = userService.getByGroupId(group.getId());
      log.info("students = {}", byGroupId);
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

  @Transactional
  public void sendEmail(AnnouncementSendInit domain) throws AddressException {
    String htmlBody = htmlToString("announcementEmail", getMailContext(domain));
    List<MailUser> users = getEmailUsers(domain);

    log.info("nb of users = {}", users.size());
    users.forEach(
        user -> {
          log.info("mail user {}", user);
        });

    List<InternetAddress> targetListAddress =
        users.stream().map(this::getInternetAddressFromUser).toList();

    InternetAddress firstAddress = targetListAddress.getFirst();

    mailer.accept(
        new Email(
            firstAddress,
            targetListAddress.subList(1, targetListAddress.size()),
            List.of(),
            domain.getTitle(),
            htmlBody,
            List.of()));
  }

  private InternetAddress getInternetAddressFromUser(MailUser user) {
    try {
      return new InternetAddress(user.email());
    } catch (AddressException e) {
      log.info("error while getting internet address", e);
      throw new RuntimeException(e);
    }
  }

  private static Context getMailContext(AnnouncementSendInit announcement) {
    Context initial = new Context();
    initial.setVariable("content", announcement.getContent());
    return initial;
  }

  @Override
  public void accept(AnnouncementSendInit announcementSendInit) {
    try {
      sendEmail(announcementSendInit);
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
