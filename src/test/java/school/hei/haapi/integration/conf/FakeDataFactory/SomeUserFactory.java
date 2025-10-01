package school.hei.haapi.integration.conf.FakeDataFactory;

import static java.util.Optional.empty;
import static school.hei.haapi.integration.conf.FakeDataProvider.someCoordinates;
import static school.hei.haapi.model.User.Role.STUDENT;

import com.github.javafaker.Faker;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.NoArgsConstructor;
import school.hei.haapi.integration.conf.FakeDataProvider;
import school.hei.haapi.model.User;

@NoArgsConstructor
public class SomeUserFactory extends User.UserBuilder {
  private static final Faker faker = new Faker();
  private Optional<String> firstName = empty();
  private Optional<User.Status> status = empty();

  public SomeUserFactory firstName(String firstName) {
    this.firstName = Optional.of(firstName);
    return this;
  }

  public SomeUserFactory status(User.Status status) {
    this.status = Optional.of(status);
    return this;
  }

  @Override
  public User build() {
    return User.builder(someCoordinates())
        .id(UUID.randomUUID().toString())
        .role(STUDENT)
        .firstName(firstName.orElseGet(faker.name()::firstName))
        .email(faker.internet().emailAddress())
        .ref(FakeDataProvider.someRef("STD"))
        .lastName(faker.name().lastName())
        .address(faker.address().fullAddress())
        .status(status.orElse(User.Status.ENABLED))
        .entranceDatetime(Instant.now())
        .build();
  }
}
