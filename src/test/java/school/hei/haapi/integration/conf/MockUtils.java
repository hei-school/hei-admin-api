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
import school.hei.haapi.endpoint.rest.model.Group;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.User;

@Component
public class MockUtils {
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
}
