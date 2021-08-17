package school.hei.haapi.integration.conf;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CallerData {
  public static final String STUDENT1_ID = "student1_id";
  public static final String STUDENT2_ID = "student2_id";
  public static final String TEACHER1_ID = "teacher1_id";
  public static final String TEACHER2_ID = "teacher2_id";
  public static final String BAD_TOKEN = "bad_token"; // null, invalid or expired
  public static final String STUDENT1_TOKEN = "student1_token";
  public static final String TEACHER1_TOKEN = "teacher1_token";

  @Getter private final String token;

  public CallerData(@Value("${test.cognito.jwt.studenttoken}") final String token) {
    this.token = token;
  }
}
