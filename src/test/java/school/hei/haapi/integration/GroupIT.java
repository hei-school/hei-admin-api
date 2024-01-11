package school.hei.haapi.integration;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.integration.conf.TestUtils.BAD_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.GROUP1_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT2_ID;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.createGroup;
import static school.hei.haapi.integration.conf.TestUtils.isValidUUID;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.conf.FacadeIT;
import school.hei.haapi.endpoint.rest.api.TeachingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CreateGroup;
import school.hei.haapi.endpoint.rest.model.Group;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.TestUtils;

@Testcontainers
class GroupIT extends FacadeIT {
  @LocalServerPort private int serverPort;

  @MockBean private SentryConf sentryConf;

  @MockBean private CognitoComponent cognitoComponentMock;

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, serverPort);
  }

  public static Group group1() {
    Group group = new Group();
    group.setId("group1_id");
    group.setName("Name of group one");
    group.setRef("GRP21001");
    group.setCreationDatetime(Instant.parse("2021-11-08T08:25:24.00Z"));
    return group;
  }

  private static Group creatableGroup() {
    Group group = new Group();
    group.setId(randomUUID().toString());
    group.setName("Name of group one");
    group.setRef("GRP" + randomUUID());
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

  private static CreateGroup someCreatableGroup(List<String> students) {
    CreateGroup createGroup = new CreateGroup();
    createGroup.setName("Some name");
    createGroup.setRef("GRP21-" + randomUUID());
    createGroup.setStudentsToAdd(students);
    return createGroup;
  }

  private static Group toGroup(CreateGroup createGroup) {
    return new Group()
        .id(createGroup.getId())
        .name(createGroup.getName())
        .creationDatetime(createGroup.getCreationDatetime())
        .ref(createGroup.getRef());
  }

  private static CreateGroup toCreateGroup(Group group) {
    return new CreateGroup()
        .id(group.getId())
        .name(group.getName())
        .creationDatetime(group.getCreationDatetime())
        .ref(group.getRef());
  }

  @BeforeEach
  public void setUp() {
    setUpCognito(cognitoComponentMock);
  }

  @Test
  void badtoken_read_ko() {
    ApiClient anonymousClient = anApiClient(BAD_TOKEN);

    TeachingApi api = new TeachingApi(anonymousClient);
    assertThrowsForbiddenException(() -> api.getGroups(1, 10));
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
    List<Group> actualGroups = api.getGroups(1, 10);

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
    TeachingApi api = new TeachingApi(manager1Client);
    CreateGroup toCreate = someCreatableGroup(new ArrayList<>());
    CreateGroup toCreate2 = someCreatableGroup(List.of(STUDENT1_ID, STUDENT2_ID));

    List<Group> actualWithoutStudents = api.createOrUpdateGroups(List.of(toCreate));
    List<Group> actualWithStudents = api.createOrUpdateGroups(List.of(toCreate2));
    List<Student> actualWithStudentsStudents =
        api.getAllStudentByGroup(actualWithStudents.get(0).getId(), 1, 10, null, null);

    assertEquals(1, actualWithoutStudents.size());
    Group createdFromToCreate0 = actualWithoutStudents.get(0);
    assertTrue(isValidUUID(createdFromToCreate0.getId()));
    toCreate.setId(createdFromToCreate0.getId());
    assertNotNull(createdFromToCreate0.getCreationDatetime());
    toCreate.setCreationDatetime(createdFromToCreate0.getCreationDatetime());
    assertEquals(createdFromToCreate0, toGroup(toCreate));

    assertEquals(2, actualWithStudentsStudents.size());
  }
}
