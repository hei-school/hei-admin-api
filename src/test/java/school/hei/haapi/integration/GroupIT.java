package school.hei.haapi.integration;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.integration.conf.utils.MockObjects.student1;
import static school.hei.haapi.integration.conf.utils.TestUtils.BAD_TOKEN;
import static school.hei.haapi.integration.conf.utils.TestUtils.GROUP1_ID;
import static school.hei.haapi.integration.conf.utils.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.utils.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.utils.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.utils.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.utils.TestUtils.group3;
import static school.hei.haapi.integration.conf.utils.TestUtils.group5;
import static school.hei.haapi.integration.conf.utils.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.utils.TestUtils.setUpS3Service;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.TeachingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CreateGroup;
import school.hei.haapi.endpoint.rest.model.Group;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.utils.TestUtils;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

@Testcontainers
@AutoConfigureMockMvc
class GroupIT extends FacadeITMockedThirdParties {
  @MockBean private EventBridgeClient eventBridgeClientMock;

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  public static Group group1() {
    Group group = new Group();
    group.setId("group1_id");
    group.setRef("G1");
    group.setName("GRP21001");
    group.setCreationDatetime(Instant.parse("2021-11-08T08:25:24.00Z"));
    group.setSize(2);
    return group;
  }

  public static Group group2() {
    Group group = new Group();
    group.setId("group2_id");
    group.setRef("G2");
    group.setName("GRP21002");
    group.setCreationDatetime(Instant.parse("2021-11-08T08:30:24.00Z"));
    group.setSize(1);
    return group;
  }

  public static Group updatedGroup3() {
    Group copyGroup3 = new Group();
    copyGroup3.setId(group3().getId());
    copyGroup3.setRef(group3().getRef());
    copyGroup3.setCreationDatetime(group3().getCreationDatetime());
    copyGroup3.setName(group3().getName());
    copyGroup3.setSize(1);
    return copyGroup3;
  }

  public static Group updatedGroup5() {
    Group copyGroup5 = new Group();
    copyGroup5.setId(group5().getId());
    copyGroup5.setRef(group5().getRef());
    copyGroup5.setCreationDatetime(group5().getCreationDatetime());
    copyGroup5.setName(group5().getName());
    copyGroup5.setSize(1);
    return copyGroup5;
  }

  public static CreateGroup someCreatableGroup(List<String> students) {
    CreateGroup createGroup = new CreateGroup();
    createGroup.setName("Some name");
    createGroup.setRef("GRP21-" + randomUUID());
    createGroup.setStudents(students);
    return createGroup;
  }

  public static Group createGroupToGroup(CreateGroup createGroup) {
    return new Group()
        .id(createGroup.getId())
        .name(createGroup.getName())
        .creationDatetime(createGroup.getCreationDatetime())
        .ref(createGroup.getRef())
        .size(createGroup.getSize() == null ? 0 : createGroup.getSize());
  }

  public static CreateGroup groupToCreateGroup(Group group) {
    return new CreateGroup()
        .id(group.getId())
        .name(group.getName())
        .creationDatetime(group.getCreationDatetime())
        .ref(group.getRef())
        .size(group.getSize());
  }

  @BeforeEach
  public void setUp() {
    setUpCognito(cognitoComponentMock);
    setUpS3Service(fileService, student1());
  }

  @Test
  void badtoken_read_ko() {
    ApiClient anonymousClient = anApiClient(BAD_TOKEN);

    TeachingApi api = new TeachingApi(anonymousClient);
    assertThrowsForbiddenException(() -> api.getGroups(null, null, 1, 10));
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
    List<Group> actualGroups = api.getGroups(null, null, 1, 10);

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
  void manager_read_ok() throws ApiException {
    ApiClient client = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(client);

    List<Group> actualGroups = api.getGroups(null, null, 1, 10);
    assertTrue(actualGroups.contains(group1()));
    assertTrue(actualGroups.contains(group2()));
    assertTrue(actualGroups.contains(updatedGroup3()));

    List<Group> groupsFilteredByRef = api.getGroups("G1", null, 1, 10);
    assertTrue(groupsFilteredByRef.contains(group1()));
    assertFalse(groupsFilteredByRef.contains(group2()));
    assertFalse(groupsFilteredByRef.contains(updatedGroup3()));

    List<Group> groupsFilteredByStudentRef = api.getGroups(null, "STD21002", 1, 10);
    assertTrue(groupsFilteredByStudentRef.contains(group1()));
    assertFalse(groupsFilteredByStudentRef.contains(group2()));
    assertFalse(groupsFilteredByStudentRef.contains(group3()));
  }

  @Test
  void manager_write_update_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(manager1Client);

    Group group =
        api.createOrUpdateGroups(
                List.of(new CreateGroup().name("name").ref("ref").creationDatetime(Instant.now())))
            .getFirst();

    List<CreateGroup> ModifyGroups = List.of(groupToCreateGroup(group).name("A new name zero"));

    List<Group> updated = api.createOrUpdateGroups(ModifyGroups);

    assertTrue(updated.contains(createGroupToGroup(ModifyGroups.getFirst())));
  }
}
