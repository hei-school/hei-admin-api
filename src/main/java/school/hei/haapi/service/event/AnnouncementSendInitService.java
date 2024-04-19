package school.hei.haapi.service.event;

import static school.hei.haapi.model.User.Status.ENABLED;
import static school.hei.haapi.service.utils.TemplateUtils.htmlToString;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import school.hei.haapi.endpoint.event.gen.AnnouncementSendInit;
import school.hei.haapi.endpoint.rest.model.Group;
import school.hei.haapi.mail.Email;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.model.User;
import school.hei.haapi.service.UserService;

@Service
@AllArgsConstructor
public class AnnouncementSendInitService implements Consumer<AnnouncementSendInit> {

  private final UserService userService;
  private final Mailer mailer;

  public List<User> getStudents(AnnouncementSendInit domain) {
    List<User> students = new ArrayList<>();
    for (Group group : domain.getGroups()) {
      students.addAll(userService.getByGroupId(group.getId()));
    }
    return students;
  }

  public void sendEmail(AnnouncementSendInit domain) throws AddressException {
    String htmlBody = htmlToString("announcementEmail", getMailContext(domain));
    List<User> users =
        switch (domain.getScope()) {
          case GLOBAL -> userService.getAll();
          case TEACHER -> userService.getByRoleAndStatus(User.Role.TEACHER, ENABLED);
          case STUDENT -> getStudents(domain);
          case MANAGER -> userService.getByRoleAndStatus(User.Role.MANAGER, ENABLED);
        };

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

  public InternetAddress getInternetAddressFromUser(User user) {
    try {
      return new InternetAddress(user.getEmail());
    } catch (AddressException e) {
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
}
