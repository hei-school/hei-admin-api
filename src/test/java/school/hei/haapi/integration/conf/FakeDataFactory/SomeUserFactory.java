package school.hei.haapi.integration.conf.FakeDataFactory;

import static school.hei.haapi.integration.conf.FakeDataProvider.someCoordinates;
import static school.hei.haapi.model.User.Role.STUDENT;

import java.time.Instant;
import java.util.UUID;
import school.hei.haapi.integration.conf.FakeDataProvider;
import school.hei.haapi.model.User;

public class SomeUserFactory extends SomeDataAbstractFactory<User, User.UserBuilder> {
  public SomeUserFactory() {
    super(
        User.builder(someCoordinates())
            .id(UUID.randomUUID().toString())
            .role(STUDENT)
            .firstName(faker.name().firstName())
            .email(faker.internet().emailAddress())
            .ref(FakeDataProvider.someRef("STD"))
            .lastName(faker.name().lastName())
            .address(faker.address().fullAddress())
            .status(User.Status.ENABLED)
            .entranceDatetime(Instant.now()));
  }

  public SomeUserFactory firstName(String firstName) {
    builder.firstName(firstName);
    return this;
  }

  public SomeUserFactory status(User.Status status) {
    builder.status(status);
    return this;
  }

  @Override
  public User build() {
    return builder.build();
  }
}
