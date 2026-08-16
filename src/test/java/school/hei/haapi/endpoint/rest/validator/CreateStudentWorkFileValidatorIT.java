package school.hei.haapi.endpoint.rest.validator;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.endpoint.rest.model.ProfessionalExperienceFileTypeEnum.WORKER_STUDENT;
import static school.hei.haapi.integration.conf.TestMocks.setUpS3Service;
import static school.hei.haapi.integration.testData.StudentTestData.axel;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.exception.ApiException;

class CreateStudentWorkFileValidatorIT extends FacadeITMockedThirdParties {
  @Autowired private CreateStudentWorkFileValidator subject;

  @BeforeEach
  void setUp() {
    setUpS3Service(fileService, axel());
  }

  @Test
  void assert_commitment_begin_is_before_end() {
    var exception =
        assertThrows(
            ApiException.class,
            () ->
                subject.acceptWorkDocumentField(
                    "filename",
                    Instant.parse("2021-11-25T08:25:24.00Z"),
                    Instant.parse("2021-11-08T08:25:24.00Z"),
                    WORKER_STUDENT));
    var actualMessage = exception.getMessage();
    var expectedMessage = "Commitment begin must be less than commitment end";

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  void assert_filename_is_given() {
    var exception =
        assertThrows(
            ApiException.class,
            () ->
                subject.acceptWorkDocumentField(
                    null,
                    Instant.parse("2021-11-25T08:25:24.00Z"),
                    Instant.parse("2021-11-08T08:25:24.00Z"),
                    WORKER_STUDENT));
    var actualMessage = exception.getMessage();
    var expectedMessage = "Filename is mandatory";

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  void assert_experience_type_is_given() {
    var exception =
        assertThrows(
            ApiException.class,
            () ->
                subject.acceptWorkDocumentField(
                    "file", Instant.parse("2021-11-25T08:25:24.00Z"), null, null));
    var actualMessage = exception.getMessage();
    var expectedMessage = "Professional experience type is mandatory";

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  void assert_commitment_begin_is_given() {
    var exception =
        assertThrows(
            ApiException.class,
            () -> subject.acceptWorkDocumentField("filename", null, null, WORKER_STUDENT));
    var actualMessage = exception.getMessage();
    var expectedMessage = "Commitment begin date is mandatory";

    assertTrue(actualMessage.contains(expectedMessage));
  }
}
