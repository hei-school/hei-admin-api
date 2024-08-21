package school.hei.haapi.service.event;

import static school.hei.haapi.service.utils.TemplateUtils.htmlToString;

import jakarta.mail.internet.InternetAddress;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import school.hei.haapi.endpoint.event.model.StudentsWithOverdueFeesReminder;
import school.hei.haapi.mail.Email;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.service.utils.Base64Converter;
import school.hei.haapi.service.utils.ClassPathResourceResolver;

@Service
@AllArgsConstructor
@Slf4j
public class StudentsWithOverdueFeesReminderService
    implements Consumer<StudentsWithOverdueFeesReminder> {
  private final Base64Converter base64Converter;
  private final ClassPathResourceResolver classPathResourceResolver;
  private final Mailer mailer;

  private Context getMailContext(StudentsWithOverdueFeesReminder students) {
    Context initial = new Context();
    Resource emailSignatureImage = classPathResourceResolver.apply("HEI_signature", ".png");

    initial.setVariable("students", students.getStudents());
    initial.setVariable("emailSignature", base64Converter.apply(emailSignatureImage));
    return initial;
  }

  @Override
  public void accept(StudentsWithOverdueFeesReminder students) {
    String htmlBody = htmlToString("studentsWithOverdueEmail", getMailContext(students));
    mailer.accept(
        new Email(
            internetAddress("contact@mail.hei.school"),
            List.of(),
            List.of(),
            "FRAIS EN RETARD - Etudiants",
            htmlBody,
            List.of()));
    log.info("Email sent...");
  }

  @SneakyThrows
  private static InternetAddress internetAddress(String email) {
    return new InternetAddress(email);
  }
}
