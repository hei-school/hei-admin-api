package school.hei.haapi.integration;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.TeachingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Group;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.BAD_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.GROUP1_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.isValidUUID;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = GroupIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class GroupIT {

  @MockBean
  private SentryConf sentryConf;

  @MockBean
  private CognitoComponent cognitoComponentMock;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, ContextInitializer.SERVER_PORT);
  }

  public static Group group1() {
    Group group = new Group();
    group.setId("group1_id");
    group.setName("Name of group one");
    group.setRef("GRP21001");
    group.setCreationDatetime(Instant.parse("2021-11-08T08:25:24.00Z"));
    return group;
  }

  public static Group group2() {
    Group group = new Group();
    group.setId("group2_id");
    group.setName("Name of group two");
    group.setRef("GRP21002");
    group.setCreationDatetime(Instant.parse("2021-11-08T08:30:24.00Z"));
    return group;
  }

  public static Group someCreatableGroup() {
    Group group = new Group();
    group.setName("Some name");
    group.setRef("GRP21-" + randomUUID());
    return group;
  }

  @BeforeEach
  public void setUp() {
    setUpCognito(cognitoComponentMock);
  }

  @Test
  void badtoken_read_ko() {
    ApiClient anonymousClient = anApiClient(BAD_TOKEN);

    TeachingApi api = new TeachingApi(anonymousClient);
    assertThrowsForbiddenException(api::getGroups);
  }

  @Test
  void badtoken_write_ko() {
    ApiClient anonymousClient = anApiClient(BAD_TOKEN);

    TeachingApi api = new TeachingApi(anonymousClient);
    assertThrowsForbiddenException(() -> api.createOrUpdateGroups(List.of()));
  }

  @Test
  void student_read_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

    TeachingApi api = new TeachingApi(student1Client);
    Group actual1 = api.getGroupById(GROUP1_ID);
    List<Group> actualGroups = api.getGroups();

    assertEquals(group1(), actual1);
    assertTrue(actualGroups.contains(group1()));
    assertTrue(actualGroups.contains(group2()));
  }

  @Test
  void student_write_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

    TeachingApi api = new TeachingApi(student1Client);
    assertThrowsForbiddenException(() -> api.createOrUpdateGroups(List.of()));
  }

  @Test
  void teacher_write_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);

    TeachingApi api = new TeachingApi(teacher1Client);
    assertThrowsForbiddenException(() -> api.createOrUpdateGroups(List.of()));
  }

  @Test
  void manager_write_create_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    Group toCreate3 = someCreatableGroup();
    Group toCreate4 = someCreatableGroup();

    TeachingApi api = new TeachingApi(manager1Client);
    List<Group> created = api.createOrUpdateGroups(List.of(toCreate3, toCreate4));

    assertEquals(2, created.size());
    Group created3 = created.get(0);
    assertTrue(isValidUUID(created3.getId()));
    toCreate3.setId(created3.getId());
    assertNotNull(created3.getCreationDatetime());
    toCreate3.setCreationDatetime(created3.getCreationDatetime());
    //
    assertEquals(created3, toCreate3);
    Group created4 = created.get(0);
    assertTrue(isValidUUID(created4.getId()));
    toCreate4.setId(created4.getId());
    assertNotNull(created4.getCreationDatetime());
    toCreate4.setCreationDatetime(created4.getCreationDatetime());
    assertEquals(created4, toCreate3);
  }

  @Test
  void manager_write_update_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(manager1Client);
    List<Group> toUpdate = api.createOrUpdateGroups(List.of(
        someCreatableGroup(),
        someCreatableGroup()));
    Group toUpdate0 = toUpdate.get(0);
    toUpdate0.setName("A new name zero");
    Group toUpdate1 = toUpdate.get(1);
    toUpdate1.setName("A new name one");

    List<Group> updated = api.createOrUpdateGroups(toUpdate);

    assertEquals(2, updated.size());
    assertTrue(updated.contains(toUpdate0));
    assertTrue(updated.contains(toUpdate1));
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
