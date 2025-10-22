package school.hei.haapi.service.event;

import static java.nio.charset.StandardCharsets.UTF_8;
import static school.hei.haapi.service.utils.FileUtils.createFileFromBytes;
import static school.hei.haapi.service.utils.TemplateUtils.htmlToString;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import school.hei.haapi.endpoint.event.model.StudentImportEvent;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.mail.Email;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.model.User;
import school.hei.haapi.model.dto.StudentImportDto;
import school.hei.haapi.service.UserService;

@Service
@AllArgsConstructor
@Slf4j
public class StudentImportEventService implements Consumer<StudentImportEvent> {
  private final UserMapper userMapper;
  private final UserService userService;
  private final Mailer mailer;

  @Override
  @Transactional
  public void accept(StudentImportEvent event) {
    var coordinatorUser = userService.getByEmail(event.getCoordinatorEmail());
    try {
      userService.saveAll(
          userMapper.toMapDomain(
              event.getStudents().stream().map(StudentImportDto::toCrupdateStudent).toList()),
          event.getDueDatetime());
    } catch (DuplicateKeyException e) {
      sendDuplicatedValueEmail(event, coordinatorUser);
      throw e;
    } catch (Exception e) {
      sendErrorEmail(event, e, coordinatorUser);
      throw e;
    }
  }

  private void sendDuplicatedValueEmail(StudentImportEvent event, User coordinatorUser) {
    try {
      var coordinatorAddress = new InternetAddress(event.getCoordinatorEmail());
      var htmlBody =
          htmlToString(
              "studentXlsxImportDuplicateValueEmail", getMailContext(event, coordinatorUser));
      log.info("Sending student import failure email...");
      mailer.accept(
          new Email(
              coordinatorAddress,
              List.of(),
              List.of(),
              "Échec de l'import des étudiants - valeurs en double",
              htmlBody,
              List.of()));
    } catch (AddressException e) {
      throw new RuntimeException(
          "Failed to send student import failure email to invalid email address: "
              + event.getCoordinatorEmail(),
          e);
    }
  }

  private Context getMailContext(StudentImportEvent event, User coordinatorUser) {
    var context = new Context();
    context.setVariable("coordinatorFirstName", coordinatorUser.getFirstName());
    return context;
  }

  private void sendErrorEmail(StudentImportEvent event, Exception e, User coordinatorUser) {
    try {
      var logFile = createFileFromBytes(e.toString().getBytes(UTF_8), "log_file", ".txt");
      var coordinatorAddress = new InternetAddress(event.getCoordinatorEmail());
      var htmlBody =
          htmlToString("studentXlsxImportErrorEmail", getMailContext(event, coordinatorUser));
      log.info("Sending student import failure email...");
      mailer.accept(
          new Email(
              coordinatorAddress,
              List.of(),
              List.of(),
              "Échec de l'import des étudiants",
              htmlBody,
              List.of(logFile)));
    } catch (AddressException ex) {
      throw new RuntimeException(
          "Failed to send student import failure email to invalid email address: "
              + event.getCoordinatorEmail(),
          ex);
    } catch (Exception ex) {
      throw new RuntimeException("Failed to send student import failure email", ex);
    }
  }
}
