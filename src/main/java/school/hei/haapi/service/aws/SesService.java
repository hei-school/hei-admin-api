package school.hei.haapi.service.aws;

import lombok.AllArgsConstructor;
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
public class SesService {
  private final SesClient client;

  public void sendEmail(String sender, String recipient,
                        String subject, String bodyHtml) {
    SendEmailRequest emailRequest = SendEmailRequest.builder()
        .source(sender)
        .destination(Destination.builder()
            .toAddresses(recipient)
            .build())
        .message(Message.builder()
            .subject(Content.builder()
                .data(subject)
                .build())
            .body(Body.builder()
                .html(Content.builder()
                    .data(bodyHtml)
                    .build())
                .build())
            .build())
        .build();
    try {
      client.sendEmail(emailRequest);
    } catch (AwsServiceException | SdkClientException exception) {
      throw new ApiException(SERVER_EXCEPTION, exception.getMessage());
    }
  }
}
