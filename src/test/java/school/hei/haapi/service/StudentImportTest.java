package school.hei.haapi.service;

import static java.time.Instant.now;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.PaymentFrequency.MONTHLY;
import static school.hei.haapi.endpoint.rest.model.PaymentFrequency.YEARLY;
import static school.hei.haapi.endpoint.rest.model.Sex.F;
import static school.hei.haapi.endpoint.rest.model.Sex.M;
import static school.hei.haapi.integration.conf.TestUtils.getMockedFile;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.StudentImportEvent;
import school.hei.haapi.endpoint.rest.security.AuthProvider;
import school.hei.haapi.endpoint.rest.security.model.Principal;
import school.hei.haapi.file.bucket.BucketComponent;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.model.User;
import school.hei.haapi.model.dto.StudentImportDto;
import school.hei.haapi.service.event.StudentImportEventService;

@Testcontainers
public class StudentImportTest extends FacadeITMockedThirdParties {
  @Autowired private UserService subject;
  @Autowired private StudentImportEventService studentImportEventService;
  @MockBean private Mailer mailer;
  @MockBean BucketComponent bucketComponent;
  @MockBean private EventProducer eventProducer;

  @Test
  void validate_import_student_xlsx_ok() {
    mockStatic(AuthProvider.class);
    when(AuthProvider.getPrincipal()).thenReturn(mockPrincipal());
    var importResult =
        subject.initStudentImportFromXlsx(getMockedFile("test-student-import", ".xlsx"), now());
    assertEquals(3, importResult.getValidStudentNumber());
  }

  @Test
  void handle_student_import_xlsx() {
    assertDoesNotThrow(() -> studentImportEventService.accept(studentImportEventMock()));
    assertNotNull(subject.getByEmail("example@mail.com"));
    assertNotNull(subject.getByEmail("example.2@mail.com"));
  }

  @Test
  void import_already_existing_email_ko() {
    assertThrows(
        DataIntegrityViolationException.class,
        () -> studentImportEventService.accept(importEventAlreadyExistingEmail()));
  }

  @Test
  void import_bad_student_ko() {
    assertThrows(Exception.class, () -> studentImportEventService.accept(badImportEvent()));
  }

  private Principal mockPrincipal() {
    return new Principal(User.builder().email("test@email.com").build(), "huh!?");
  }

  private StudentImportEvent studentImportEventMock() {
    return StudentImportEvent.builder()
        .coordinatorEmail("test+manager1@hei.school")
        .dueDatetime(now())
        .students(
            List.of(
                StudentImportDto.builder()
                    .address("Test Adress")
                    .phone("012333334344")
                    .ref("STD12345-1")
                    .lastName("LastName")
                    .firstName("FirstName")
                    .sex(M)
                    .email("example@mail.com")
                    .entranceDatetime(now())
                    .paymentFrequency(MONTHLY)
                    .birthDate(LocalDate.of(2004, 2, 1))
                    .build(),
                StudentImportDto.builder()
                    .address("Test Adress 2")
                    .ref("STD12345-2")
                    .lastName("LastName")
                    .firstName("FirstName")
                    .sex(F)
                    .email("example.2@mail.com")
                    .entranceDatetime(now())
                    .paymentFrequency(YEARLY)
                    .build()))
        .build();
  }

  private StudentImportEvent importEventAlreadyExistingEmail() {
    return StudentImportEvent.builder()
        .coordinatorEmail("test+manager1@hei.school")
        .dueDatetime(now())
        .students(
            List.of(
                StudentImportDto.builder()
                    .address("Test Adress")
                    .phone("012333334344")
                    .ref("STD12345-1")
                    .lastName("LastName")
                    .firstName("FirstName")
                    .sex(M)
                    .email("test+ryan@hei.school")
                    .entranceDatetime(now())
                    .paymentFrequency(MONTHLY)
                    .birthDate(LocalDate.of(2004, 2, 1))
                    .build()))
        .build();
  }

  private StudentImportEvent badImportEvent() {
    return StudentImportEvent.builder()
        .coordinatorEmail("test+manager1@hei.school")
        .dueDatetime(now())
        .students(
            List.of(
                StudentImportDto.builder()
                    .address("Test Adress")
                    .phone("012333334344")
                    .ref("STD12345-1")
                    .lastName("LastName")
                    .firstName("FirstName")
                    .sex(M)
                    .email("test+ryan@hei.school")
                    .entranceDatetime(now())
                    .paymentFrequency(MONTHLY)
                    .birthDate(LocalDate.of(2004, 2, 1))
                    .build()))
        .build();
  }
}
