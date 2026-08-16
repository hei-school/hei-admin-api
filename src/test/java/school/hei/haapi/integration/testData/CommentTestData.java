package school.hei.haapi.integration.testData;

import static java.util.UUID.randomUUID;

import school.hei.haapi.model.Comment;
import school.hei.haapi.model.User;

public class CommentTestData {
  public static Comment aComment(User subject, User observer, String content) {
    return Comment.builder()
        .id(randomUUID().toString())
        .subject(subject)
        .observer(observer)
        .content(content)
        .build();
  }
}
