package school.hei.haapi.endpoint.rest.validator;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.endpoint.rest.model.ProfessionalExperienceFileTypeEnum.WORKER_STUDENT;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.MockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.exception.ApiException;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = CreateStudentWorkFileValidatorIT.ContextInitializer.class)
@AutoConfigureMockMvc
class CreateStudentWorkFileValidatorIT extends MockedThirdParties {
  @Autowired private CreateStudentWorkFileValidator subject;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(
        token, CreateStudentWorkFileValidatorIT.ContextInitializer.SERVER_PORT);
  }

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
    setUpS3Service(fileService, student1());
  }

  @Test
  void assert_commitment_begin_is_before_end() {
    Exception exception =
        assertThrows(
            ApiException.class,
            () ->
                subject.acceptWorkDocumentField(
                    "filename",
                    Instant.parse("2021-11-25T08:25:24.00Z"),
                    Instant.parse("2021-11-08T08:25:24.00Z"),
                    WORKER_STUDENT));
    String actualMessage = exception.getMessage();
    String expectedMessage = "Commitment begin must be less than commitment end";

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  void assert_filename_is_given() {
    Exception exception =
        assertThrows(
            ApiException.class,
            () ->
                subject.acceptWorkDocumentField(
                    null,
                    Instant.parse("2021-11-25T08:25:24.00Z"),
                    Instant.parse("2021-11-08T08:25:24.00Z"),
                    WORKER_STUDENT));
    String actualMessage = exception.getMessage();
    String expectedMessage = "Filename is mandatory";

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  void assert_experience_type_is_given() {
    Exception exception =
        assertThrows(
            ApiException.class,
            () ->
                subject.acceptWorkDocumentField(
                    "file", Instant.parse("2021-11-25T08:25:24.00Z"), null, null));
    String actualMessage = exception.getMessage();
    String expectedMessage = "Professional experience type is mandatory";

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  void assert_commitment_begin_is_given() {
    Exception exception =
        assertThrows(
            ApiException.class,
            () -> subject.acceptWorkDocumentField("filename", null, null, WORKER_STUDENT));
    String actualMessage = exception.getMessage();
    String expectedMessage = "Commitment begin date is mandatory";

    assertTrue(actualMessage.contains(expectedMessage));
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
