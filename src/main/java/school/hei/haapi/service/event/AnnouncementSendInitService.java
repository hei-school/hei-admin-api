package school.hei.haapi.service.event;

import static java.lang.Math.min;
import static school.hei.haapi.model.User.Role.MANAGER;
import static school.hei.haapi.model.User.Role.TEACHER;
import static school.hei.haapi.model.User.Status.ENABLED;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.gen.AnnouncementEmailSendRequested;
import school.hei.haapi.endpoint.event.gen.AnnouncementEmailSendRequested.MailUser;
import school.hei.haapi.endpoint.event.gen.AnnouncementSendInit;
import school.hei.haapi.model.User;
import school.hei.haapi.model.notEntity.Group;
import school.hei.haapi.service.UserService;

@Service
@AllArgsConstructor
@Slf4j
public class AnnouncementSendInitService implements Consumer<AnnouncementSendInit> {
  private final UserService userService;
  private final EventProducer eventProducer;

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
    return users.stream().map(MailUser::from).toList();
  }

  @Override
  public void accept(AnnouncementSendInit domain) {
    List<MailUser> mailReceivers = getEmailUsers(domain);
    log.info("nb of email recipients = {}", mailReceivers.size());
    MailUser adminMailReceiver = new MailUser("admin_id", "contact@mail.hei.school");
    var adminEmailRequest = AnnouncementEmailSendRequested.from(domain, adminMailReceiver);
    List<AnnouncementEmailSendRequested> events =
        mailReceivers.stream().map(to -> AnnouncementEmailSendRequested.from(domain, to)).toList();
    var allReceivers = new ArrayList<>(events);
    allReceivers.add(adminEmailRequest);
    List<Object> list = Arrays.asList(allReceivers.toArray());
    if (list.size() > EventProducer.Conf.MAX_PUT_EVENT_ENTRIES) {
      group(list).forEach(eventProducer);
    } else {
      eventProducer.accept(list);
    }
  }

  // TODO: rm when poja ListGrouper is implemented in EventProducer
  private List<List<Object>> group(List<Object> list) {
    List<List<Object>> groupedList = new ArrayList<>();
    int size = list.size();

    for (int i = 0; i < size; i += EventProducer.Conf.MAX_PUT_EVENT_ENTRIES) {
      int end = min(size, i + EventProducer.Conf.MAX_PUT_EVENT_ENTRIES);
      groupedList.add(new ArrayList<>(list.subList(i, end)));
    }

    return groupedList;
  }
}
