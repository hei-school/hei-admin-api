package school.hei.haapi.integration.conf;

import static java.util.concurrent.TimeUnit.DAYS;

import com.github.javafaker.Faker;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Group;

@Component
public class MockUtils {
  private final Faker faker = new Faker();

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
}
