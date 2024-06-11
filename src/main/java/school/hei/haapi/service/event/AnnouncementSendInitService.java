package school.hei.haapi.service.event;

import static java.lang.Math.min;
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
  private static final int MAX_SES_RECIPIENT_SIZE = 50;

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
    log.info("Send email : {}", htmlBody);
    List<MailUser> users = getEmailUsers(domain);

    log.info("nb of email recipients = {}", users.size());
    List<InternetAddress> targetListAddresses =
        users.stream().map(this::getInternetAddressFromUser).toList();

    // mail admin first then CC other receivers in order to check for data arrival
    InternetAddress adminMailReceiver = new InternetAddress("contact@mail.hei.school");
    listGroups(targetListAddresses)
        .forEach(
            listGroup -> {
              mailer.accept(
                  new Email(
                      adminMailReceiver,
                      listGroup,
                      List.of(),
                      domain.getTitle(),
                      htmlBody,
                      List.of()));
              log.info(
                  "mailSent {} {} to {} users",
                  domain.getTitle(),
                  domain.getScope(),
                  listGroup.size());
            });
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

  private List<List<InternetAddress>> listGroups(List<InternetAddress> addresses) {
    if (addresses.size() <= MAX_SES_RECIPIENT_SIZE) {
      return List.of(addresses);
    }
    List<List<InternetAddress>> groupedList = new ArrayList<>();
    int size = addresses.size();

    int groupSize = MAX_SES_RECIPIENT_SIZE;
    for (int i = 0; i < size; i += groupSize) {
      int end = min(size, i + groupSize);
      groupedList.add(new ArrayList<>(addresses.subList(i, end)));
    }

    return groupedList;
  }
}
