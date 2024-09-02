package school.hei.haapi.integration;

import static org.junit.Assert.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.endpoint.rest.model.LetterStatus.PENDING;
import static school.hei.haapi.endpoint.rest.model.LetterStatus.RECEIVED;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.TestUtils.*;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.LettersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Letter;
import school.hei.haapi.endpoint.rest.model.UpdateLettersStatus;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.MockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = LetterIT.ContextInitializer.class)
@AutoConfigureMockMvc
@Slf4j
public class LetterIT extends MockedThirdParties {
  @MockBean EventBridgeClient eventBridgeClientMock;
  @Autowired ObjectMapper objectMapper;

  @BeforeEach
  public void setUp() {
    setUpCognito(cognitoComponentMock);
    setUpEventBridge(eventBridgeClientMock);
    setUpS3Service(fileService, student1());
  }

  @Test
  void manager_read_ok() throws ApiException {
    ApiClient apiClient = anApiClient(MANAGER1_TOKEN);
    LettersApi api = new LettersApi(apiClient);

    List<Letter> actual = api.getLetters(1, 15, null, null);
    assertTrue(actual.contains(letter1()));
    assertTrue(actual.contains(letter2()));
    assertTrue(actual.contains(letter3()));

    List<Letter> filteredByStudentRef = api.getLetters(1, 15, "STD21001", null);
    assertTrue(filteredByStudentRef.contains(letter1()));
    assertTrue(filteredByStudentRef.contains(letter2()));
    assertFalse(filteredByStudentRef.contains(letter3()));

    List<Letter> filteredByLetterRef = api.getLetters(1, 15, null, "letter1_ref");
    assertTrue(filteredByLetterRef.contains(letter1()));
    assertFalse(filteredByLetterRef.contains(letter2()));
    assertFalse(filteredByLetterRef.contains(letter3()));
  }

  @Test
  void manager_read_by_id() throws ApiException {
    ApiClient apiClient = anApiClient(MANAGER1_TOKEN);
    LettersApi api = new LettersApi(apiClient);

    Letter actual = api.getLetterById(LETTER1_ID);
    assertEquals(letter1(), actual);
  }

  @Test
  void manager_read_students_letter() throws ApiException {
    ApiClient apiClient = anApiClient(MANAGER1_TOKEN);
    LettersApi api = new LettersApi(apiClient);

    List<Letter> actual = api.getLettersByStudentId(STUDENT1_ID, 1, 15);
    assertTrue(actual.contains(letter1()));
    assertTrue(actual.contains(letter2()));
    assertFalse(actual.contains(letter3()));

    List<Letter> actual2 = api.getLettersByStudentId(STUDENT2_ID, 1, 15);
    assertFalse(actual2.contains(letter1()));
    assertFalse(actual2.contains(letter2()));
    assertTrue(actual2.contains(letter3()));
  }

  @Test
  void manager_create_and_update_students_letter()
      throws IOException, InterruptedException, ApiException {
    ApiClient apiClient = anApiClient(MANAGER1_TOKEN);
    LettersApi api = new LettersApi(apiClient);

    HttpResponse<InputStream> response =
        uploadLetter(
            LetterIT.ContextInitializer.SERVER_PORT,
            MANAGER1_TOKEN,
            STUDENT1_ID,
            "Certificat",
            "file");
    Letter createdLetter = objectMapper.readValue(response.body(), Letter.class);
    log.info("created : {}", createdLetter);
    assertEquals(createdLetter.getDescription(), "Certificat");
    assertTrue(createdLetter.getFilePath().contains("file"));
    assertEquals(PENDING, createdLetter.getStatus());

    List<Letter> updatedLetters =
        api.updateLettersStatus(
            List.of(new UpdateLettersStatus().id(createdLetter.getId()).status(RECEIVED)));
    log.info("updated : {}", updatedLetters);
    Letter updatedLetter = updatedLetters.getFirst();
    assertEquals(RECEIVED, updatedLetter.getStatus());
    assertNotNull(updatedLetter.getApprovalDatetime());
    assertEquals(createdLetter.getId(), updatedLetter.getId());
  }

  @Test
  void student_read_self_ok() throws ApiException {
    ApiClient apiClient = anApiClient(STUDENT1_TOKEN);
    LettersApi api = new LettersApi(apiClient);

    List<Letter> actual = api.getLettersByStudentId(STUDENT1_ID, 1, 15);
    assertTrue(actual.contains(letter1()));
    assertTrue(actual.contains(letter2()));
    assertFalse(actual.contains(letter3()));
  }

  @Test
  void student_forbidden_endpoint() throws ApiException {
    ApiClient apiClient = anApiClient(STUDENT1_TOKEN);
    LettersApi api = new LettersApi(apiClient);

    assertThrowsForbiddenException(() -> api.getLettersByStudentId(STUDENT2_ID, 1, 15));
    assertThrowsForbiddenException(
        () ->
            api.updateLettersStatus(
                List.of(new UpdateLettersStatus().id("test").status(RECEIVED))));
    // TODO: The test should pass
    // assertThrowsForbiddenException(() -> api.createLetter(STUDENT2_ID,"null","null",
    // getMockedFile("img", ".png")));
  }

  @Test
  void student_upload_own_letter_ok() throws ApiException, IOException, InterruptedException {
    HttpResponse<InputStream> response =
        uploadLetter(
            LetterIT.ContextInitializer.SERVER_PORT,
            STUDENT1_TOKEN,
            STUDENT1_ID,
            "Certificat",
            "file");
    Letter createdLetter = objectMapper.readValue(response.body(), Letter.class);
    assertEquals(createdLetter.getDescription(), "Certificat");
    assertTrue(createdLetter.getFilePath().contains("file"));
    assertEquals(PENDING, createdLetter.getStatus());
  }

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, LetterIT.ContextInitializer.SERVER_PORT);
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
