package school.hei.haapi.service.aws;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;
import software.amazon.awssdk.services.ses.model.SesException;


@Service
@AllArgsConstructor
@Slf4j
public class SesService {
  private final SesClient client;

  public SendEmailResponse sendEmail(String sender, String recipient,
                                     String subject, String bodyHtml) {

    Destination destination = Destination.builder()
        .toAddresses(recipient)
        .build();

    Content content = Content.builder()
        .data(bodyHtml)
        .build();

    Content subj = Content.builder()
        .data(subject)
        .build();

    Body body = Body.builder()
        .html(content)
        .build();

    Message message = Message.builder()
        .subject(subj)
        .body(body)
        .build();

    SendEmailRequest emailRequest = SendEmailRequest.builder()
        .destination(destination)
        .message(message)
        .source(sender)
        .build();
    try {
      log.info("Trying to send email to " + recipient);
      client.sendEmail(emailRequest);
      log.info("Sent successfully ! Next ...");
    } catch (SesException e) {
      log.error(e.awsErrorDetails().errorMessage());
      throw new RuntimeException(e);
    }

    return SendEmailResponse.builder()
        .messageId("Email sent !")
        .build();
  }

}
