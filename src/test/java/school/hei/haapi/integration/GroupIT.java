package school.hei.haapi.integration;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.TestUtils.BAD_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT2_ID;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.cloneGroupNoTimestamp;
import static school.hei.haapi.integration.conf.TestUtils.group3;
import static school.hei.haapi.integration.conf.TestUtils.group5;
import static school.hei.haapi.integration.conf.TestUtils.isValidUUID;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognitoAndCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;
import static school.hei.haapi.integration.test_data.GroupTestData.createGroupFlow;
import static school.hei.haapi.integration.test_data.GroupTestData.g1;
import static school.hei.haapi.integration.test_data.GroupTestData.g2;
import static school.hei.haapi.integration.test_data.StudentTestData.axel;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.GroupsApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.mapper.GroupMapper;
import school.hei.haapi.endpoint.rest.model.CreateGroup;
import school.hei.haapi.endpoint.rest.model.Group;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.GroupFlowRepository;
import school.hei.haapi.repository.GroupRepository;
import school.hei.haapi.repository.UserRepository;

// TODO: isolate this test's data (please!) and add an @AfterEach when soft delete is added to all
// entities
@Testcontainers
@AutoConfigureMockMvc
class GroupIT extends FacadeITMockedThirdParties {
  private List<String> groupIds = new ArrayList<>();
  private school.hei.haapi.model.Group groupG1;
  private school.hei.haapi.model.Group groupG2;
  private User studentAxel;
  private GroupFlow groupFlowsAxel;
  private List<String> studentIds = new ArrayList<>();
  private List<String> grooupFlowIds = new ArrayList<>();

  @Autowired private GroupRepository groupRepository;
  @Autowired private GroupMapper groupMapper;
  @Autowired private UserRepository userRepository;
  @Autowired private GroupFlowRepository groupFlowRepository;

  private void setUpTestData() {
    studentAxel = axel();
    groupG1 = g1();
    groupG2 = g2();
    groupFlowsAxel = createGroupFlow(studentAxel, groupG1);

    userRepository.save(studentAxel);
    groupRepository.saveAll(List.of(groupG1, groupG2));
    groupFlowRepository.save(groupFlowsAxel);

    studentIds.add(studentAxel.getId());
    groupIds.addAll(List.of(groupG1.getId(), groupG2.getId()));
    grooupFlowIds.add(groupFlowsAxel.getId());
  }

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
    group.setSize(0);
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
    setUpCognitoAndCasdoor(casdoorAuthServiceMock, cognitoComponentMock, certificateLoaderMock);
    setUpS3Service(fileService, student1());
    setUpTestData();
  }

  @Test
  void badtoken_read_ko() {
    ApiClient anonymousClient = anApiClient(BAD_TOKEN);
    GroupsApi api = new GroupsApi(anonymousClient);
    assertThrowsForbiddenException(() -> api.getGroups(null, null, 1, 10));
  }

  @Test
  void badtoken_write_ko() {
    ApiClient anonymousClient = anApiClient(BAD_TOKEN);

    GroupsApi api = new GroupsApi(anonymousClient);
    assertThrowsForbiddenException(() -> api.createOrUpdateGroups(List.of()));
  }

  // TODO: fix potential interference due to pagination, for now pageSize 250 groups per page is
  // more than enough
  @Test
  void student_read_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    GroupsApi api = new GroupsApi(student1Client);
    Group actualG1 = api.getGroupById(groupG1.getId());
    List<Group> actualGroups = api.getGroups(null, null, 1, 250);

    // TODO: should be way cleaner if we could deal with @CreationTimestamp using PrePersist to
    // avoid the auto-now
    var restGroupG1 = groupMapper.toRest(groupG1);
    var restGroupG2 = groupMapper.toRest(groupG2);
    var actualG1NoTimestamp = cloneGroupNoTimestamp(actualG1);
    var actualGroupsWithoutCreationTimestamp =
        actualGroups.stream().map(TestUtils::cloneGroupNoTimestamp).toList();
    var restGroupG1NoTimestamp = cloneGroupNoTimestamp(restGroupG1);
    var restGroupG2NoTimestamp = cloneGroupNoTimestamp(restGroupG2);

    assertEquals(actualG1NoTimestamp, restGroupG1NoTimestamp);
    assertEquals(restGroupG1NoTimestamp, actualG1NoTimestamp);
    assertTrue(actualGroupsWithoutCreationTimestamp.contains(restGroupG1NoTimestamp));
    assertTrue(actualGroupsWithoutCreationTimestamp.contains(restGroupG2NoTimestamp));
  }

  @Test
  void student_write_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

    GroupsApi api = new GroupsApi(student1Client);
    assertThrowsForbiddenException(() -> api.createOrUpdateGroups(List.of()));
  }

  @Test
  void teacher_write_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);

    GroupsApi api = new GroupsApi(teacher1Client);
    assertThrowsForbiddenException(() -> api.createOrUpdateGroups(List.of()));
  }

  // TODO: fix potential interference with pagination, for now pageSize 250 groups per page is more
  // than enough
  @Test
  void manager_read_ok() throws ApiException {
    ApiClient client = anApiClient(MANAGER1_TOKEN);
    GroupsApi api = new GroupsApi(client);
    var restGroupG1 = groupMapper.toRest(groupG1);
    var restGroupG2 = groupMapper.toRest(groupG2);

    // TODO: make it much cleaner once you deal with @CreationTimestamp in the model
    List<Group> actualGroups = api.getGroups(null, null, 1, 250);
    var actualGroupsWithoutTimestamp =
        actualGroups.stream().map(TestUtils::cloneGroupNoTimestamp).toList();
    var restGroupG1WithoutTimestamp = cloneGroupNoTimestamp(restGroupG1);
    var restGroupG2WithoutTimestamp = cloneGroupNoTimestamp(restGroupG2);
    assertTrue(
        actualGroupsWithoutTimestamp.contains(restGroupG1WithoutTimestamp),
        "Expected " + actualGroupsWithoutTimestamp + " to contain " + restGroupG1WithoutTimestamp);
    assertTrue(actualGroupsWithoutTimestamp.contains(restGroupG2WithoutTimestamp));

    List<Group> groupsFilteredByRef = api.getGroups(groupG1.getRef(), null, 1, 250);
    var groupsFilteredByRefWithoutTimestamp =
        groupsFilteredByRef.stream().map(TestUtils::cloneGroupNoTimestamp).toList();
    assertTrue(groupsFilteredByRefWithoutTimestamp.contains(restGroupG1WithoutTimestamp));
    assertFalse(groupsFilteredByRefWithoutTimestamp.contains(restGroupG2WithoutTimestamp));
    assertEquals(1, groupsFilteredByRef.size());

    List<Group> groupsFilteredByStudentRef = api.getGroups(null, studentAxel.getRef(), 1, 250);
    var groupsFilteredByStudentRefWithoutTimestamp =
        groupsFilteredByStudentRef.stream().map(TestUtils::cloneGroupNoTimestamp).toList();
    assertTrue(groupsFilteredByStudentRefWithoutTimestamp.contains(restGroupG1WithoutTimestamp));
    assertFalse(groupsFilteredByStudentRefWithoutTimestamp.contains(restGroupG2WithoutTimestamp));
  }

  @Test
  void manager_write_create_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    CreateGroup toCreate3 = someCreatableGroup(new ArrayList<>());
    CreateGroup toCreate4 = someCreatableGroup(new ArrayList<>());
    CreateGroup toCreate5 = someCreatableGroup(List.of(STUDENT1_ID, STUDENT2_ID));

    GroupsApi api = new GroupsApi(manager1Client);
    List<Group> created = api.createOrUpdateGroups(List.of(toCreate3, toCreate4));
    List<Group> createdWithStudent = api.createOrUpdateGroups(List.of(toCreate5));
    List<Student> students =
        api.getStudentsByGroupId(createdWithStudent.getFirst().getId(), 1, 10, null);

    assertEquals(2, created.size());
    Group created3 = created.getFirst();
    assertTrue(isValidUUID(created3.getId()));
    toCreate3.setId(created3.getId());
    assertNotNull(created3.getCreationDatetime());
    toCreate3.setCreationDatetime(created3.getCreationDatetime());

    assertEquals(created3, createGroupToGroup(toCreate3));
    Group created4 = created.getFirst();
    assertTrue(isValidUUID(created4.getId()));
    toCreate4.setId(created4.getId());
    assertNotNull(created4.getCreationDatetime());
    toCreate4.setCreationDatetime(created4.getCreationDatetime());
    assertEquals(created4, createGroupToGroup(toCreate3));

    assertEquals(2, students.size());
  }

  @Test
  void manager_write_update_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    GroupsApi api = new GroupsApi(manager1Client);

    Group group =
        api.createOrUpdateGroups(
                List.of(new CreateGroup().name("name").ref("ref").creationDatetime(Instant.now())))
            .getFirst();

    List<CreateGroup> ModifyGroups = List.of(groupToCreateGroup(group).name("A new name zero"));

    List<Group> updated = api.createOrUpdateGroups(ModifyGroups);

    assertTrue(updated.contains(createGroupToGroup(ModifyGroups.getFirst())));
  }
}
