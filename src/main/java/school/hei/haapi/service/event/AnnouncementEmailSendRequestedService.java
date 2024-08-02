package school.hei.haapi.service.event;

import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;
import static school.hei.haapi.service.utils.TemplateUtils.htmlToString;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import school.hei.haapi.endpoint.event.model.AnnouncementEmailSendRequested;
import school.hei.haapi.mail.Email;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.model.exception.ApiException;
import school.hei.haapi.service.utils.Base64Converter;
import school.hei.haapi.service.utils.ClassPathResourceResolver;

@Service
@AllArgsConstructor
@Slf4j
public class AnnouncementEmailSendRequestedService
    implements Consumer<AnnouncementEmailSendRequested> {
  private final Mailer mailer;
  private final Base64Converter base64Converter;
  private final ClassPathResourceResolver classPathResourceResolver;

  private InternetAddress getInternetAddressFromUser(AnnouncementEmailSendRequested.MailUser user) {
    try {
      return new InternetAddress(user.email());
    } catch (AddressException e) {
      log.info("bad email format for {} {}", user.id(), user.email());
      throw new ApiException(SERVER_EXCEPTION, e);
    }
  }

  private Context getMailContext(AnnouncementEmailSendRequested domain) {
    Context initial = new Context();
    Resource emailSignatureImage = classPathResourceResolver.apply("Signature-HEI-v2", ".png");

    initial.setVariable("fullName", domain.getSenderFullName());
    initial.setVariable("id", domain.getAnnouncementId());
    initial.setVariable("emailSignature", base64Converter.apply(emailSignatureImage));
    return initial;
  }

  @Override
  public void accept(AnnouncementEmailSendRequested domain) {
    var body = htmlToString("announcementEmail", getMailContext(domain));
    InternetAddress to = getInternetAddressFromUser(domain.getTo());
    mailer.accept(new Email(to, List.of(), List.of(), domain.getTitle(), body, List.of()));
    log.info("mailSent {} {} to {}", domain.getTitle(), domain.getScope(), domain.getTo());
  }
}
