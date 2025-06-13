package school.hei.haapi.endpoint.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.endpoint.rest.model.Scope.GLOBAL;
import static school.hei.haapi.endpoint.rest.model.Scope.STUDENT;
import static school.hei.haapi.endpoint.rest.model.Scope.TEACHER;
import static school.hei.haapi.integration.conf.TestUtils.GROUP2_ID;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.event.model.AnnouncementSendInit;
import school.hei.haapi.endpoint.rest.model.Scope;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.MockedThirdParties;
import school.hei.haapi.model.notEntity.Group;
import school.hei.haapi.service.GroupService;
import school.hei.haapi.service.event.AnnouncementSendInitService;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = AnnouncementSendInitServiceIT.ContextInitializer.class)
@AutoConfigureMockMvc
class AnnouncementSendInitServiceIT extends MockedThirdParties {
  @Autowired AnnouncementSendInitService announcementSendInitService;
  @MockBean EventProducer eventProducerMock;
  @Autowired GroupService groupService;

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

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
