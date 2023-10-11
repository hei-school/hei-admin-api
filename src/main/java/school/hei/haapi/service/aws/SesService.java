package school.hei.haapi.service.aws;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.exception.ApiException;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

@Component
@Service
@AllArgsConstructor
public class SesService {
  private final SesClient client;

  public void sendEmail(String sender, String contact, String recipient,
                        String subject, String htmlBody) {
    SendEmailRequest emailRequest = SendEmailRequest.builder()
        .source(sender)
        .destination(destination -> destination.toAddresses(recipient).bccAddresses(contact))
        .message(message -> {
          message.subject(content -> content.data(subject));
          message.body(body -> body.html(content -> content.data(htmlBody)));
        })
        .build();
    try {
      client.sendEmail(emailRequest);
    } catch (AwsServiceException | SdkClientException exception) {
      throw new ApiException(SERVER_EXCEPTION, exception.getMessage());
    }
  }
}
