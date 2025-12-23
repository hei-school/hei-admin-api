package school.hei.haapi.service.event;

import static java.time.temporal.ChronoUnit.MINUTES;
import static school.hei.haapi.service.utils.TemplateUtils.htmlToString;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import school.hei.haapi.endpoint.event.model.YearlyResultTranscriptGeneration;
import school.hei.haapi.file.bucket.BucketComponent;
import school.hei.haapi.mail.Email;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.model.User;
import school.hei.haapi.model.YearlyResultGenerationRequest;
import school.hei.haapi.service.GradeResultService;

@Slf4j
@Service
@RequiredArgsConstructor
public class YearlyResultTranscriptGenerationService
    implements Consumer<YearlyResultTranscriptGeneration> {
  private final GradeResultService gradeResultService;
  private final Mailer mailer;
  private final BucketComponent bucketComponent;

  @Override
  @Transactional
  public void accept(YearlyResultTranscriptGeneration yearlyResultTranscriptGeneration) {
    var request =
        gradeResultService.uploadYearlyResultTranscript(
            yearlyResultTranscriptGeneration.getUserId(),
            yearlyResultTranscriptGeneration.getYearlyResult());
    try {
      sendGeneratedEmail(yearlyResultTranscriptGeneration.getPrincipal(), request);
    } catch (Exception e) {
      log.error("Cannot send yearly result generation email for : {}", request.getFileName());
    }
  }

  private Context getContext(User principal, YearlyResultGenerationRequest request) {
    var context = new Context();
    context.setVariable("principal_fullname", principal.getFullName());
    context.setVariable("transcript_filename", request.getFileName());
    context.setVariable(
        "transcript_url",
        bucketComponent.presign(request.getFileInfo().getFilePath(), Duration.of(30, MINUTES)));
    return context;
  }

  private void sendGeneratedEmail(User user, YearlyResultGenerationRequest request)
      throws AddressException {
    var context = getContext(user, request);
    var mailBody = htmlToString("generatedTranscriptEmail", context);
    var email =
        new Email(
            new InternetAddress(user.getEmail()),
            List.of(),
            List.of(),
            request.getFileName() + " générée",
            mailBody,
            List.of());
    mailer.accept(email);
  }
}
