package school.hei.haapi.endpoint.event;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static school.hei.haapi.endpoint.rest.model.Scope.GLOBAL;
import static school.hei.haapi.endpoint.rest.model.Scope.STUDENT;
import static school.hei.haapi.endpoint.rest.model.Scope.TEACHER;
import static school.hei.haapi.integration.testData.GroupTestData.createGroupFlow;
import static school.hei.haapi.integration.testData.GroupTestData.g1;
import static school.hei.haapi.integration.testData.StudentTestData.axel;
import static school.hei.haapi.integration.testData.TeacherTestData.toky;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.ApiException;
import school.hei.haapi.repository.GroupFlowRepository;
import school.hei.haapi.repository.GroupRepository;
import school.hei.haapi.repository.UserRepository;
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
  @Autowired UserRepository userRepository;
  @Autowired GroupRepository groupRepository;
  @Autowired GroupFlowRepository groupFlowRepository;

  private User student;
  private User teacher;
  private Group group;
  private GroupFlow studentJoinsGroup;

  @BeforeEach
  void setUp() {
    student = userRepository.save(axel());
    teacher = userRepository.save(toky());
    group = groupRepository.save(g1());
    studentJoinsGroup = groupFlowRepository.save(createGroupFlow(student, group));
  }

  @AfterEach
  void tearDown() {
    groupFlowRepository.deleteById(studentJoinsGroup.getId());
    groupRepository.deleteById(group.getId());
    userRepository.deleteAll(List.of(student, teacher));
  }

  static AnnouncementSendInit announcement(
      Scope scope, List<school.hei.haapi.model.notEntity.Group> groups) {
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
    verify(eventProducerMock, atLeastOnce()).accept(any());
  }

  @Test
  void should_invoke_eventproducer_teacher_scope() {
    announcementSendInitService.accept(announcement(TEACHER));

    verify(eventProducerMock, atLeastOnce()).accept(any());
  }

  @Test
  void should_invoke_eventproducer_student_scope() {
    announcementSendInitService.accept(
        announcement(
            STUDENT,
            groupService.getAllById(List.of(group.getId())).stream()
                .map(
                    group ->
                        new school.hei.haapi.model.notEntity.Group(
                            group.getId(), group.getRef(), group.getName()))
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
