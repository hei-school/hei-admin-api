package school.hei.haapi.endpoint.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.event.model.StudentsWithOverdueFeesReminder;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.MockedThirdParties;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.service.event.StudentWithOverdueFeesReminderService;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = StudentsWithOverdueReminderServiceIT.ContextInitializer.class)
@AutoConfigureMockMvc
class StudentsWithOverdueReminderServiceIT extends MockedThirdParties {

  @Autowired StudentWithOverdueFeesReminderService subject;
  @MockBean Mailer mailerMock;

  static StudentsWithOverdueFeesReminder actual() {
    return StudentsWithOverdueFeesReminder.builder()
        .id("test_id")
        .students(
            List.of(
                StudentsWithOverdueFeesReminder.StudentWithOverdueFees.builder()
                    .ref("STD21001")
                    .feeComment("Comment")
                    .build()))
        .build();
  }
  ;

  @Test
  void should_invoke_event_producer() {
    subject.accept(actual());

    verify(mailerMock, times(1)).accept(any());
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
