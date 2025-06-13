package school.hei.haapi.integration.conf;

import static java.util.concurrent.TimeUnit.DAYS;
import static school.hei.haapi.endpoint.rest.model.FeeCategory.L1;
import static school.hei.haapi.endpoint.rest.model.FeeFrequency.MONTHLY;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.PENDING;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.TUITION;

import com.github.javafaker.Faker;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.EventType;
import school.hei.haapi.endpoint.rest.model.Group;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.User;

/**
 * Utility class for generating realistic fake data for testing purposes
 *
 * <p><b>Important:</b> Add/Modify data generation methods to this class as needs are identified
 */
@Component
public class FakeDataProvider {
  private static final Faker faker = new Faker();

  public Group createGroup() {
    return new Group()
        .name(faker.lorem().sentence(10))
        .ref(faker.lorem().characters(10))
        .creationDatetime(faker.date().past(30, DAYS).toInstant());
  }

  public List<Group> someCreatableGroupList(int nbOfGroup) {
    List<Group> groupList = new ArrayList<>();
    for (int i = 0; i < nbOfGroup; i++) {
      groupList.add(createGroup());
    }
    return groupList;
  }

  public Fee someFee(User student) {
    return Fee.builder()
        .id(UUID.randomUUID().toString())
        .comment(faker.lorem().sentence(10))
        .student(student)
        .totalAmount(0)
        .remainingAmount(0)
        .status(PENDING)
        .updatedAt(Instant.now())
        .creationDatetime(Instant.now())
        .dueDatetime(Instant.now())
        .category(L1)
        .frequency(MONTHLY)
        .type(TUITION)
        .build();
  }

  public Event someEvent() {
    var beginDatetime =
        faker.date().between(faker.date().past(200, DAYS), faker.date().past(100, DAYS));
    return new Event(
        UUID.randomUUID().toString(),
        faker.options().option(EventType.class),
        faker.lorem().sentence(2),
        faker.lorem().sentence(10),
        null,
        false,
        beginDatetime.toInstant(),
        faker.date().between(beginDatetime, faker.date().past(10, DAYS)).toInstant(),
        null,
        new Course("", "", "", 0, 0, List.of()),
        List.of());
  }
}
