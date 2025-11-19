package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static school.hei.haapi.integration.conf.TestUtils.getMockedFile;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.rest.security.AuthProvider;
import school.hei.haapi.endpoint.rest.security.model.Principal;
import school.hei.haapi.file.bucket.BucketComponent;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.model.User;

public class GradeImportTest extends FacadeITMockedThirdParties {
  @Autowired private GradeService subject;
  @Autowired private GradeImportEventService GradeImportEventService;
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

  private static Principal mockPrincipal() {
    return new Principal(User.builder().email("test@email.com").build(), "huh!?");
  }
}
