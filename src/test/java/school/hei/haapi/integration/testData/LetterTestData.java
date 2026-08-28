package school.hei.haapi.integration.testData;

import static java.util.UUID.randomUUID;

import school.hei.haapi.endpoint.rest.model.LetterStatus;
import school.hei.haapi.model.Letter;
import school.hei.haapi.model.User;

public class LetterTestData {
  public static Letter aLetter(User user, String description, LetterStatus status) {
    return Letter.builder()
        .id(randomUUID().toString())
        .user(user)
        .description(description)
        .status(status)
        .ref("letter_" + randomUUID())
        .filePath("/LETTERBOX/%s/%s.pdf".formatted(user.getRef(), randomUUID()))
        .build();
  }
}
