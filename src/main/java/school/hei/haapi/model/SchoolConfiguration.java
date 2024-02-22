package school.hei.haapi.model;

import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
public class SchoolConfiguration extends User {
  private final String id = "school_id";
  private final String firstName = "HEI";
  private final String lastName = "Haute Ecole d Informatique";
  private final String email = "contact@hei.school";
  private final String ref = "HEI";
  private final User.Status status = User.Status.ENABLED;
  private final User.Sex sex = User.Sex.M;
  private final LocalDate birthDate = LocalDate.parse("2000-01-01");
  private final Instant entranceDatetime = Instant.parse("2021-11-08T08:25:24.00Z");
  private final String phone = "0322411123";
  private final String adress = "Adr 1";
  private final User.Role role = User.Role.MANAGER;
}
