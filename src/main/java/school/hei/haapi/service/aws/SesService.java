package school.hei.haapi.service.aws;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.exception.ApiException;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

@Component
@Service
@AllArgsConstructor
@Slf4j
public class SesService {
  private final SesClient client;

  public void sendEmail(String sender, String recipient,
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
      client.sendEmail(emailRequest);
    } catch (AwsServiceException | SdkClientException e) {
      throw new ApiException(SERVER_EXCEPTION, e.getMessage());
    }

  }

}
