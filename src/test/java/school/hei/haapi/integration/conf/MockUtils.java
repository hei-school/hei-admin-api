package school.hei.haapi.integration.conf;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.YEARS;
import static java.util.UUID.randomUUID;
import static school.hei.haapi.endpoint.rest.model.EnableStatus.ENABLED;
import static school.hei.haapi.model.User.Role.STUDENT;

import com.github.javafaker.Faker;
import java.time.Instant;
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
import school.hei.haapi.endpoint.rest.model.CreateEvent;
import school.hei.haapi.endpoint.rest.model.CrupdateStudent;
import school.hei.haapi.endpoint.rest.model.CrupdateTeacher;
import school.hei.haapi.endpoint.rest.model.EventType;
import school.hei.haapi.endpoint.rest.model.Group;
import school.hei.haapi.endpoint.rest.model.Sex;
import school.hei.haapi.model.User;

@Component
public class MockUtils {
  private final Faker faker;
  @Autowired UserMapper userMapper;

  MockUtils() {
    faker = new Faker();
  }

  public CreateEvent someCreatableEvent(
      EventType eventType, String planerId, Instant beginDatetime, Instant endDatetime) {
    return new CreateEvent()
        .id("event" + randomUUID() + "_id")
        .courseId(TestUtils.COURSE1_ID)
        .beginDatetime(beginDatetime)
        .endDatetime(endDatetime)
        .description("Another event")
        .eventType(eventType)
        .plannerId(planerId)
        .groups(List.of(TestUtils.createGroupIdentifier(TestUtils.group1())));
  }

  private Date createSomeEntranceDateTime(Date userBirthdate, int minimumAge) {
    return faker
        .date()
        .between(
            Date.from(
                userBirthdate
                    .toInstant()
                    .atZone(ZoneOffset.UTC)
                    .toLocalDateTime()
                    .plusYears(minimumAge)
                    .toInstant(UTC)),
            DateTime.now().toDate());
  }

  private Date createSomeBirthDateBetween(int minimumAge, int maximumAge) {
    DateTime now = DateTime.now();
    return faker
        .date()
        .between(now.minusYears(maximumAge).toDate(), now.minusYears(minimumAge).toDate());
  }

  public List<CrupdateTeacher> someCreatableTeacherList(int nbOfTeacher) {
    List<CrupdateTeacher> teacherList = new ArrayList<>();
    for (int i = 0; i < nbOfTeacher; i++) {
      teacherList.add(someCreatableTeacher());
    }
    return teacherList;
  }

  public CrupdateTeacher someCreatableTeacher() {
    var firstName = faker.name().firstName();
    Date birthDate = createSomeBirthDateBetween(20, 35);
    return new CrupdateTeacher()
        .firstName(firstName)
        .lastName(faker.name().lastName())
        .email("%s%d@hei.school".formatted(firstName, faker.number().randomNumber(10, true)))
        .ref("TCR" + faker.number().randomNumber(5, true))
        .phone(faker.phoneNumber().phoneNumber())
        .status(ENABLED)
        .sex(Sex.valueOf(faker.options().option(Sex.M.getValue(), Sex.F.getValue())))
        .birthDate(birthDate.toInstant().atOffset(UTC).toLocalDate())
        .entranceDatetime(createSomeEntranceDateTime(birthDate, 20).toInstant())
        .coordinates(TestUtils.coordinatesWithNullValues())
        .address("Adr X");
  }

  public User createSomeUser(User.Role role, User.Status status) {
    var userBirthdate = createSomeBirthDateBetween(18, 25);
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
        .entranceDatetime(createSomeEntranceDateTime(userBirthdate, 18).toInstant())
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
