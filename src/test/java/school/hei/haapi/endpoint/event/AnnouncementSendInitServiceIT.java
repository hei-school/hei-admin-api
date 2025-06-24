package school.hei.haapi.endpoint.event;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static school.hei.haapi.endpoint.rest.model.Scope.GLOBAL;
import static school.hei.haapi.endpoint.rest.model.Scope.STUDENT;
import static school.hei.haapi.endpoint.rest.model.Scope.TEACHER;
import static school.hei.haapi.integration.conf.TestUtils.GROUP2_ID;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.event.model.AnnouncementEmailSendRequested;
import school.hei.haapi.endpoint.event.model.AnnouncementSendInit;
import school.hei.haapi.endpoint.rest.model.Scope;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.model.exception.ApiException;
import school.hei.haapi.model.notEntity.Group;
import school.hei.haapi.service.GroupService;
import school.hei.haapi.service.event.AnnouncementEmailSendRequestedService;
import school.hei.haapi.service.event.AnnouncementSendInitService;

@Testcontainers
@AutoConfigureMockMvc
class AnnouncementSendInitServiceIT extends FacadeITMockedThirdParties {
  @Autowired AnnouncementSendInitService announcementSendInitService;
  @MockBean EventProducer eventProducerMock;
  @Autowired GroupService groupService;
  @Autowired AnnouncementEmailSendRequestedService announcementEmailSendRequestedService;
  @MockBean Mailer mailer;

  static AnnouncementSendInit announcement(Scope scope, List<Group> groups) {
    return AnnouncementSendInit.builder()
        .title("Title")
        .scope(scope)
        .id("test_id")
        .groups(groups)
        .senderFullName("John Doe")
        .build();
  }

  static AnnouncementSendInit announcement(Scope scope) {
    return announcement(scope, List.of());
  }

  @Test
  void should_invoke_eventproducer() {
    announcementSendInitService.accept(announcement(GLOBAL));

    // 3 depends on actual data
    verify(eventProducerMock, times(3)).accept(any());
  }

  @Test
  void should_invoke_eventproducer_teacher_scope() {
    announcementSendInitService.accept(announcement(TEACHER));

    verify(eventProducerMock, times(1)).accept(any());
  }

  @Test
  void should_invoke_eventproducer_student_scope() {
    announcementSendInitService.accept(
        announcement(
            STUDENT,
            groupService.getAllById(List.of(GROUP2_ID)).stream()
                .map(group -> new Group(group.getId(), group.getRef(), group.getName()))
                .toList()));

    verify(eventProducerMock, times(1)).accept(any());
  }

  @Test
  void should_send_email() {
    announcementEmailSendRequestedService.accept(
        announcementEmailSendRequest("id", "email@gmail.com"));

    verify(mailer, times(1)).accept(any());
  }

  @Test
  void should_not_send_email_if_bad_email() {
    var announcementEmailSendRequest = announcementEmailSendRequest("fake_id", "");
    assertThrows(
        ApiException.class,
        () -> announcementEmailSendRequestedService.accept(announcementEmailSendRequest));

    verify(mailer, times(0)).accept(any());
  }

  private static AnnouncementEmailSendRequested announcementEmailSendRequest(
      String userId, String email) {
    return new AnnouncementEmailSendRequested(
        "",
        "",
        GLOBAL,
        "",
        "",
        AnnouncementEmailSendRequested.MailUser.builder().id(userId).email(email).build());
  }
}
