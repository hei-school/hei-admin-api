package school.hei.haapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.haapi.service.aws.SesService;
import software.amazon.awssdk.services.ses.SesClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class SesServiceTest {
  private final static String sender = "contact@hei.school";
  private final static String recipient = "test+ryan@hei.school";
  private final static String subject = "Testing sending email with SES!";
  private final static String success = "Email sent !";
  private final static String bodyHtml = "<h1>Hello World !</h1>";
  SesService ses;
  SesClient client;

  @BeforeEach
  void setUp() {
    client = mock(SesClient.class);
    ses = new SesService(client);
  }

  @Test
  void student_get_email_response_ok() {
    assertEquals(success,
        ses.sendEmail(sender, recipient, subject, bodyHtml)
            .messageId());
  }

}
