package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static school.hei.haapi.integration.conf.TestUtils.getMockedFile;

import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.GradeImportEvent;
import school.hei.haapi.endpoint.rest.security.AuthProvider;
import school.hei.haapi.endpoint.rest.security.model.Principal;
import school.hei.haapi.file.bucket.BucketComponent;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.model.User;
import school.hei.haapi.model.dto.GradeImportDto;
import school.hei.haapi.model.exception.BadRequestException;

public class GradeImportTest extends FacadeITMockedThirdParties {
  @Autowired private GradeService subject;
  @Autowired private GradeImportEventService gradeImportEventService;
  @MockBean Mailer mailer;
  @MockBean BucketComponent bucketComponent;
  @MockBean private EventProducer eventProducer;

  @BeforeAll
  static void setUp() {
    mockStatic(AuthProvider.class);
    when(AuthProvider.getPrincipal()).thenReturn(mockPrincipal());
  }

  @Test
  void validate_import_student_grade_ok() {
    var importResult =
        subject.initStudentExamGradeImportFromXlsx(
            getMockedFile("test-grade-import", ".xlsx"), "exam1_id");
    assertEquals(2, importResult.getValidStudentExamGradeNumber());
  }

  @Test
  void validate_bad_student_import_ko() {
    assertThrows(
        BadRequestException.class,
        () ->
            subject.initStudentExamGradeImportFromXlsx(
                getMockedFile("test-bad-student-grade-import", ".xlsx"), "exam1_id"));
  }

  @Test
  void handle_student_import_xlsx() {
    assertDoesNotThrow(() -> gradeImportEventService.accept(gradeImportEventMock()));
    assertNotNull(subject.getGradeByExamIdAndStudentRef("exam1_id", "STD21002"));
  }

  @Test
  void import_bad_student_ko() {
    assertThrows(Exception.class, () -> gradeImportEventService.accept(badImportEvent()));
  }

  private static Principal mockPrincipal() {
    return new Principal(User.builder().email("test@email.com").build(), "huh!?");
  }

  private static GradeImportEvent gradeImportEventMock() {
    return GradeImportEvent.builder()
        .examId("exam1_id")
        .coordinatorEmail("test+manager1@hei.school")
        .grades(List.of(GradeImportDto.builder().ref("STD21002").score(12.5).build()))
        .build();
  }

  private static GradeImportEvent badImportEvent() {
    return GradeImportEvent.builder()
        .examId("exam1_id")
        .coordinatorEmail("test+manager1@hei.school")
        .grades(List.of(GradeImportDto.builder().ref("STD21002").score(12.5).build()))
        .build();
  }
}
