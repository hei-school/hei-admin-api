package school.hei.haapi.service;

import static java.nio.charset.StandardCharsets.UTF_8;
import static school.hei.haapi.service.utils.FileUtils.createFileFromBytes;
import static school.hei.haapi.service.utils.TemplateUtils.htmlToString;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import school.hei.haapi.endpoint.event.model.GradeImportEvent;
import school.hei.haapi.endpoint.rest.mapper.GradeMapper;
import school.hei.haapi.mail.Email;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.model.User;

@Service
@AllArgsConstructor
@Slf4j
public class GradeImportEventService implements Consumer<GradeImportEvent> {
  private final GradeService gradeService;
  private final UserService userService;
  private final GradeMapper gradeMapper;
  private final Mailer mailer;

  @Override
  public void accept(GradeImportEvent event) {
    var coordinatorUser = userService.getByEmail(event.getCoordinatorEmail());
    try {
      gradeService.createParticipantGrade(
          gradeMapper.toDomainList(event.getGrades(), event.getExamId()));
    } catch (DataIntegrityViolationException e) {
      sendDuplicatedValueEmail(event, coordinatorUser);
      throw e;
    } catch (Exception e) {
      sendErrorEmail(event, e, coordinatorUser);
      throw e;
    }
  }

  private void sendDuplicatedValueEmail(GradeImportEvent event, User coordinatorUser) {
    try {
      var coordinatorAddress = new InternetAddress(event.getCoordinatorEmail());
      var htmlBody =
          htmlToString("gradeXlsxImportDuplicateValueEmail", getMailContext(coordinatorUser));
      log.info("Sending grade import failure email...");
      mailer.accept(
          new Email(
              coordinatorAddress,
              List.of(),
              List.of(),
              "Échec de l'import des notes - valeurs en double",
              htmlBody,
              List.of()));
    } catch (AddressException e) {
      throw new RuntimeException(
          "Failed to send grade import failure email to invalid email address: "
              + event.getCoordinatorEmail(),
          e);
    }
  }

  private Context getMailContext(User coordinatorUser) {
    var context = new Context();
    context.setVariable("coordinatorFirstName", coordinatorUser.getFirstName());
    return context;
  }

  private void sendErrorEmail(GradeImportEvent event, Exception e, User coordinatorUser) {
    try {
      var logFile = createFileFromBytes(e.toString().getBytes(UTF_8), "log_file", ".txt");
      var coordinatorAddress = new InternetAddress(event.getCoordinatorEmail());
      var htmlBody = htmlToString("gradeXlsxImportErrorEmail", getMailContext(coordinatorUser));
      log.info("Sending grade import failure email...");
      mailer.accept(
          new Email(
              coordinatorAddress,
              List.of(),
              List.of(),
              "Échec de l'import des notes",
              htmlBody,
              List.of(logFile)));
    } catch (AddressException ex) {
      throw new RuntimeException(
          "Failed to send grade import failure email to invalid email address: "
              + event.getCoordinatorEmail(),
          ex);
    } catch (Exception ex) {
      throw new RuntimeException("Failed to send grade import failure email", ex);
    }
  }
}
