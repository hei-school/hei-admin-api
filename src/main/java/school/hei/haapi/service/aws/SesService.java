package school.hei.haapi.service.aws;

import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SesException;

import java.io.UnsupportedEncodingException;

@Service
@AllArgsConstructor
public class SesService {
  private final SesClient client;

  public void sendEmail(String sender, String recipient,
                        String subject, String bodyHtml) throws UnsupportedEncodingException, MessagingException {

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

    try{
      client.sendEmail(emailRequest);
    }
    catch (SesException e) {
      System.err.println(e.awsErrorDetails().errorMessage());
      System.exit(1);
    }

  }

}
