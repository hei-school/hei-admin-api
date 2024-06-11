package school.hei.haapi.endpoint.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.endpoint.rest.model.Scope.GLOBAL;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.event.gen.AnnouncementSendInit;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.MockedThirdParties;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.service.event.AnnouncementSendInitService;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = AnnouncementSendEmailInvokerIT.ContextInitializer.class)
@AutoConfigureMockMvc
class AnnouncementSendEmailInvokerIT extends MockedThirdParties {
  @Autowired AnnouncementSendInitService announcementSendInitService;
  ;
  @MockBean Mailer mailer;
  @MockBean EventProducer producer;

  static AnnouncementSendInit announcement() {
    return AnnouncementSendInit.builder()
        .title("Title")
        .scope(GLOBAL)
        .senderFullName("John Doe")
        .build();
  }

  @Test
  void announcementEmail_invokes_corresponding_service() {
    announcementSendInitService.accept(announcement());

    verify(mailer, times(1)).accept(any());
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
