package school.hei.haapi.integration.conf;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.YEARS;
import static school.hei.haapi.model.User.Role.STUDENT;

import com.github.javafaker.Faker;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.endpoint.rest.model.CrupdateStudent;
import school.hei.haapi.endpoint.rest.model.Group;
import school.hei.haapi.model.User;

@Component
public class MockUtils {
  private final Faker faker;
  @Autowired UserMapper userMapper;

  MockUtils() {
    faker = new Faker();
  }

  public User createSomeUser(User.Role role, User.Status status) {
    DateTime now = DateTime.now();
    var userBirthdate =
        faker.date().between(now.minusYears(25).toDate(), now.minusYears(18).toDate());
    return User.builder()
        .firstName(faker.name().firstName())
        .lastName(faker.name().lastName())
        .email(faker.internet().emailAddress())
        .phone(faker.phoneNumber().phoneNumber())
        .latitude(faker.random().nextDouble())
        .longitude(faker.random().nextDouble())
        .address(faker.address().streetAddress())
        .sex(Math.random() >= 0.3 ? User.Sex.M : User.Sex.F)
        .status(status)
        .role(role)
        .ref(role + "-" + faker.number().randomNumber(5, true))
        .nic(String.valueOf(faker.number().randomNumber(12, true)))
        .birthDate(userBirthdate.toInstant().atOffset(UTC).toLocalDate())
        .entranceDatetime(
            faker
                .date()
                .between(
                    Date.from(
                        userBirthdate
                            .toInstant()
                            .atZone(ZoneOffset.UTC)
                            .toLocalDateTime()
                            .plusYears(18)
                            .toInstant(UTC)),
                    now.toDate())
                .toInstant())
        .build();
  }

  public List<User> createSomeUsers(int count, User.Role role, User.Status status) {
    return IntStream.range(0, count).mapToObj(i -> createSomeUser(role, status)).toList();
  }

  public Group createSomeGroup() {
    return new Group()
        .name(faker.lorem().sentence(3))
        .ref("GRP" + faker.number().randomNumber(5, true))
        .creationDatetime(faker.date().past(4, TimeUnit.of(YEARS)).toInstant());
  }

  public List<Group> createSomeGroupList(int nbOfGroup) {
    List<Group> groupList = new ArrayList<>();
    for (int i = 0; i < nbOfGroup; i++) {
      groupList.add(createSomeGroup());
    }
    return groupList;
  }

  public CrupdateStudent someCreatableStudent() {
    return TestUtils.toCrupdateStudent(
        userMapper.toRestStudent(createSomeUser(STUDENT, User.Status.ENABLED)));
  }
}
