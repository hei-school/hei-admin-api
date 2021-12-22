package school.hei.haapi.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.GroupsApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Group;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import java.time.Instant;
import java.util.List;

import static java.time.Instant.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.isValidUUID;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = GroupIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class GroupIT {

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = 8081;

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }

  private static ApiClient aClient(String token) {
    return TestUtils.aClient(token, ContextInitializer.SERVER_PORT);
  }

  @MockBean
  private CognitoComponent cognitoComponent;

  @BeforeEach
  public void setUp() {
    setUpCognito(cognitoComponent);
  }

  @Test
  void student_can_read_group1() throws ApiException {
    ApiClient student1Client = aClient(TestUtils.STUDENT1_TOKEN);

    GroupsApi api = new GroupsApi(student1Client);
    Group group1 = api.getGroupById(TestUtils.GROUP1_ID);

    Group expectedGroup1 = new Group();
    expectedGroup1.setId("group1_id");
    expectedGroup1.setName("Name of group one");
    expectedGroup1.setRef("GRP21001");
    expectedGroup1.setCreationDatetime(Instant.parse("2021-11-08T08:25:24.00Z"));
    assertEquals(expectedGroup1, group1);
  }

  @Test
  void student_can_read_groups() throws ApiException {
    ApiClient student1Client = aClient(TestUtils.STUDENT1_TOKEN);

    GroupsApi api = new GroupsApi(student1Client);
    List<Group> groups = api.getGroups();

    assertEquals(2, groups.size());
    assertEquals("group1_id", groups.get(0).getId());
    assertEquals("group2_id", groups.get(1).getId());
  }

  @Test
  void student_can_not_write_groups() {
    ApiClient student1Client = aClient(TestUtils.STUDENT1_TOKEN);

    GroupsApi api = new GroupsApi(student1Client);
    assertThrowsApiException(
        () ->  api.createOrUpdateGroups(List.of()),
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Only managers can write groups\"}");
  }

  @Test
  void teacher_can_not_write_groups() {
    ApiClient teacher1Client = aClient(TestUtils.TEACHER1_TOKEN);

    GroupsApi api = new GroupsApi(teacher1Client);
    assertThrowsApiException(
        () ->  api.createOrUpdateGroups(List.of()),
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Only managers can write groups\"}");
  }

  @Test
  void manager_can_create_groups() throws ApiException {
    ApiClient manager1Client = aClient(TestUtils.MANAGER1_TOKEN);
    Group toCreate3 = new Group();
    toCreate3.setName("Name of group three");
    toCreate3.setRef("G3");
    Group toCreate4 = new Group();
    toCreate4.setName("Name of group four");
    toCreate4.setRef("G4");

    GroupsApi api = new GroupsApi(manager1Client);
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
  void manager_can_update_groups() throws ApiException {
    ApiClient manager1Client = aClient(TestUtils.MANAGER1_TOKEN);
    Group toUpdate1 = new Group();
    toUpdate1.setId("group1_id");
    toUpdate1.setName("New name of group one");
    toUpdate1.setRef("G1p");
    toUpdate1.setCreationDatetime(now());
    Group toUpdate2 = new Group();
    toUpdate2.setId("group2_id");
    toUpdate2.setName("New name of group two");
    toUpdate2.setRef("G2p");
    toUpdate2.setCreationDatetime(now());

    GroupsApi api = new GroupsApi(manager1Client);
    List<Group> updated = api.createOrUpdateGroups(List.of(toUpdate1, toUpdate2));

    assertEquals(2, updated.size());
    assertEquals(toUpdate1, updated.get(0));
    assertEquals(toUpdate2, updated.get(1));
  }
}
