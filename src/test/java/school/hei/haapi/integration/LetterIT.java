package school.hei.haapi.integration;

import static org.junit.Assert.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.endpoint.rest.model.LetterStatus.*;
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

    List<Letter> actual = api.getLetters(1, 15, null, null, null, null).getData();
    assertTrue(actual.contains(letter1()));
    assertTrue(actual.contains(letter2()));
    assertTrue(actual.contains(letter3()));

    List<Letter> filteredByStudentRef = api.getLetters(1, 15, "STD21001", null, null, null).getData();
    assertTrue(filteredByStudentRef.contains(letter1()));
    assertTrue(filteredByStudentRef.contains(letter2()));
    assertFalse(filteredByStudentRef.contains(letter3()));

    List<Letter> filteredByLetterRef = api.getLetters(1, 15, null, "letter1_ref", null, null).getData();
    assertTrue(filteredByLetterRef.contains(letter1()));
    assertFalse(filteredByLetterRef.contains(letter2()));

    List<Letter> actual3 = api.getLetters(1, 15, null, null, PENDING, null).getData();
    assertFalse(actual3.contains(letter1()));
    assertTrue(actual3.contains(letter2()));
    assertTrue(actual3.contains(letter3()));
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

    List<Letter> actual = api.getLettersByStudentId(STUDENT1_ID, 1, 15, null);
    assertTrue(actual.contains(letter1()));
    assertTrue(actual.contains(letter2()));
    assertFalse(actual.contains(letter3()));

    List<Letter> actual2 = api.getLettersByStudentId(STUDENT2_ID, 1, 15, null);
    assertFalse(actual2.contains(letter1()));
    assertFalse(actual2.contains(letter2()));
    assertTrue(actual2.contains(letter3()));

    List<Letter> actual3 = api.getLettersByStudentId(STUDENT1_ID, 1, 15, PENDING);
    assertFalse(actual3.contains(letter1()));
    assertTrue(actual3.contains(letter2()));
    assertFalse(actual3.contains(letter3()));
  }

  @Test
  void manager_create_and_update_students_letter()
      throws IOException, InterruptedException, ApiException {
    ApiClient apiClient = anApiClient(MANAGER1_TOKEN);
    LettersApi api = new LettersApi(apiClient);

    HttpResponse<InputStream> toBeReceived =
        uploadLetter(
            LetterIT.ContextInitializer.SERVER_PORT,
            MANAGER1_TOKEN,
            STUDENT1_ID,
            "Certificat",
            "file");
    Letter createdLetter1 = objectMapper.readValue(toBeReceived.body(), Letter.class);
    assertEquals(createdLetter1.getDescription(), "Certificat");
    assertEquals(PENDING, createdLetter1.getStatus());

    HttpResponse<InputStream> toBeRejected =
            uploadLetter(
                    LetterIT.ContextInitializer.SERVER_PORT,
                    MANAGER1_TOKEN,
                    STUDENT1_ID,
                    "A rejeter",
                    "file");

    Letter createdLetter2 = objectMapper.readValue(toBeRejected.body(), Letter.class);
    assertEquals(createdLetter2.getDescription(), "A rejeter");
    assertEquals(PENDING, createdLetter2.getStatus());

    List<Letter> updatedLetters =
        api.updateLettersStatus(
            List.of(new UpdateLettersStatus().id(createdLetter1.getId()).status(RECEIVED), new UpdateLettersStatus().id(createdLetter2.getId()).status(REJECTED).reasonForRefusal("Mauvais format")));

    Letter updatedLetter1 = updatedLetters.getFirst();
    assertEquals(RECEIVED, updatedLetter1.getStatus());
    assertNotNull(updatedLetter1.getApprovalDatetime());
    assertEquals(createdLetter1.getId(), updatedLetter1.getId());

    Letter updatedLetter2 = updatedLetters.get(1);
    assertEquals(REJECTED, updatedLetter2.getStatus());
    assertNotNull(updatedLetter2.getApprovalDatetime());
    assertEquals(createdLetter2.getId(), updatedLetter2.getId());
  }

  @Test
  void student_read_self_ok() throws ApiException {
    ApiClient apiClient = anApiClient(STUDENT1_TOKEN);
    LettersApi api = new LettersApi(apiClient);

    List<Letter> actual = api.getLettersByStudentId(STUDENT1_ID, 1, 15, null);
    assertTrue(actual.contains(letter1()));
    assertTrue(actual.contains(letter2()));
    assertFalse(actual.contains(letter3()));
  }

  @Test
  void student_forbidden_endpoint() throws ApiException {
    ApiClient apiClient = anApiClient(STUDENT1_TOKEN);
    LettersApi api = new LettersApi(apiClient);

    assertThrowsForbiddenException(() -> api.getLettersByStudentId(STUDENT2_ID, 1, 15, null));
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
