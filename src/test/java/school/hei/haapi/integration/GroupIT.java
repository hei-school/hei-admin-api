package school.hei.haapi.integration;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.ApiAssertions.cloneGroupNoTimestamp;
import static school.hei.haapi.integration.conf.ApiAssertions.isValidUUID;
import static school.hei.haapi.integration.conf.TestAuth.tokenFor;
import static school.hei.haapi.integration.conf.TestMocks.setUpS3Service;
import static school.hei.haapi.integration.conf.TestUtils.BAD_TOKEN;
import static school.hei.haapi.integration.testData.GroupTestData.createGroupFlow;
import static school.hei.haapi.integration.testData.GroupTestData.g1;
import static school.hei.haapi.integration.testData.GroupTestData.g2;
import static school.hei.haapi.integration.testData.ManagerTestData.hasina;
import static school.hei.haapi.integration.testData.StudentTestData.axel;
import static school.hei.haapi.integration.testData.StudentTestData.freddy;
import static school.hei.haapi.integration.testData.TeacherTestData.toky;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import school.hei.haapi.endpoint.rest.api.GroupsApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.mapper.GroupMapper;
import school.hei.haapi.endpoint.rest.model.CreateGroup;
import school.hei.haapi.integration.conf.ApiAssertions;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.GroupFlowRepository;
import school.hei.haapi.repository.GroupRepository;
import school.hei.haapi.repository.UserRepository;

class GroupIT extends FacadeITMockedThirdParties {
  @Autowired private GroupRepository groupRepository;
  @Autowired private GroupMapper groupMapper;
  @Autowired private UserRepository userRepository;
  @Autowired private GroupFlowRepository groupFlowRepository;

  private Group groupG1;
  private Group groupG2;
  private User studentAxel;
  private User studentFreddy;
  private User managerHasina;
  private User teacherToky;
  private GroupFlow axelJoinsG1;

  /** Groups the tests create through the API, swept in tearDown. */
  private final List<String> createdGroupIds = new ArrayList<>();

  private String studentToken;
  private String managerToken;
  private String teacherToken;

  private void setUpTestData() {
    studentAxel = userRepository.save(axel());
    studentFreddy = userRepository.save(freddy());
    managerHasina = userRepository.save(hasina());
    teacherToky = userRepository.save(toky());

    groupG1 = groupRepository.save(g1());
    groupG2 = groupRepository.save(g2());
    axelJoinsG1 = groupFlowRepository.save(createGroupFlow(studentAxel, groupG1));
  }

  @BeforeEach
  public void setUp() {
    setUpTestData();
    setUpS3Service(fileService, studentAxel);

    studentToken = tokenFor(casdoorAuthServiceMock, studentAxel);
    managerToken = tokenFor(casdoorAuthServiceMock, managerHasina);
    teacherToken = tokenFor(casdoorAuthServiceMock, teacherToky);
  }

  @AfterEach
  void tearDown() {
    List<String> ownedGroupIds = new ArrayList<>(createdGroupIds);
    ownedGroupIds.addAll(List.of(groupG1.getId(), groupG2.getId()));

    groupFlowRepository.deleteAll(
        groupFlowRepository.findAll().stream()
            .filter(f -> ownedGroupIds.contains(f.getGroup().getId()))
            .toList());
    groupRepository.deleteAllById(ownedGroupIds);
    createdGroupIds.clear();
    userRepository.deleteAll(List.of(studentAxel, studentFreddy, managerHasina, teacherToky));
  }

  private GroupsApi apiAs(String token) {
    return new GroupsApi(anApiClient(token));
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  private static CreateGroup someCreatableGroup(List<String> students) {
    return new CreateGroup().name("Some name").ref("GRP21-" + randomUUID()).students(students);
  }

  private static school.hei.haapi.endpoint.rest.model.Group createGroupToGroup(
      CreateGroup createGroup) {
    return new school.hei.haapi.endpoint.rest.model.Group()
        .id(createGroup.getId())
        .name(createGroup.getName())
        .creationDatetime(createGroup.getCreationDatetime())
        .ref(createGroup.getRef())
        .size(createGroup.getSize() == null ? 0 : createGroup.getSize());
  }

  private static CreateGroup groupToCreateGroup(school.hei.haapi.endpoint.rest.model.Group group) {
    return new CreateGroup()
        .id(group.getId())
        .name(group.getName())
        .creationDatetime(group.getCreationDatetime())
        .ref(group.getRef())
        .size(group.getSize());
  }

  @Test
  void badtoken_read_ko() {
    var api = apiAs(BAD_TOKEN);

    assertThrowsForbiddenException(() -> api.getGroups(null, null, 1, 10));
  }

  @Test
  void badtoken_write_ko() {
    var api = apiAs(BAD_TOKEN);

    assertThrowsForbiddenException(() -> api.createOrUpdateGroups(List.of()));
  }

  @Test
  void student_read_ok() throws ApiException {
    var api = apiAs(studentToken);

    var actualG1 = api.getGroupById(groupG1.getId());
    var actualGroups = api.getGroups(null, null, 1, 250);

    // creationDatetime is a @CreationTimestamp, so it is compared out
    var restGroupG1NoTimestamp = cloneGroupNoTimestamp(groupMapper.toRest(groupG1));
    var restGroupG2NoTimestamp = cloneGroupNoTimestamp(groupMapper.toRest(groupG2));
    var actualGroupsNoTimestamp =
        actualGroups.stream().map(ApiAssertions::cloneGroupNoTimestamp).toList();

    assertEquals(restGroupG1NoTimestamp, cloneGroupNoTimestamp(actualG1));
    assertTrue(actualGroupsNoTimestamp.contains(restGroupG1NoTimestamp));
    assertTrue(actualGroupsNoTimestamp.contains(restGroupG2NoTimestamp));
  }

  @Test
  void student_write_ko() {
    var api = apiAs(studentToken);

    assertThrowsForbiddenException(() -> api.createOrUpdateGroups(List.of()));
  }

  @Test
  void teacher_write_ko() {
    var api = apiAs(teacherToken);

    assertThrowsForbiddenException(() -> api.createOrUpdateGroups(List.of()));
  }

  @Test
  void manager_read_ok() throws ApiException {
    var api = apiAs(managerToken);
    var restGroupG1NoTimestamp = cloneGroupNoTimestamp(groupMapper.toRest(groupG1));
    var restGroupG2NoTimestamp = cloneGroupNoTimestamp(groupMapper.toRest(groupG2));

    var actualGroups = api.getGroups(null, null, 1, 250);
    var actualGroupsNoTimestamp =
        actualGroups.stream().map(ApiAssertions::cloneGroupNoTimestamp).toList();
    assertTrue(
        actualGroupsNoTimestamp.contains(restGroupG1NoTimestamp),
        "Expected " + actualGroupsNoTimestamp + " to contain " + restGroupG1NoTimestamp);
    assertTrue(actualGroupsNoTimestamp.contains(restGroupG2NoTimestamp));

    var byRef = api.getGroups(groupG1.getRef(), null, 1, 250);
    var byRefNoTimestamp = byRef.stream().map(ApiAssertions::cloneGroupNoTimestamp).toList();
    assertEquals(1, byRef.size());
    assertTrue(byRefNoTimestamp.contains(restGroupG1NoTimestamp));
    assertFalse(byRefNoTimestamp.contains(restGroupG2NoTimestamp));

    var byStudentRef = api.getGroups(null, studentAxel.getRef(), 1, 250);
    var byStudentRefNoTimestamp =
        byStudentRef.stream().map(ApiAssertions::cloneGroupNoTimestamp).toList();
    assertTrue(byStudentRefNoTimestamp.contains(restGroupG1NoTimestamp));
    assertFalse(byStudentRefNoTimestamp.contains(restGroupG2NoTimestamp));
  }

  @Test
  void manager_write_create_ok() throws ApiException {
    var api = apiAs(managerToken);
    CreateGroup emptyOne = someCreatableGroup(new ArrayList<>());
    CreateGroup emptyTwo = someCreatableGroup(new ArrayList<>());
    var withStudents = someCreatableGroup(List.of(studentAxel.getId(), studentFreddy.getId()));

    var created = api.createOrUpdateGroups(List.of(emptyOne, emptyTwo));
    var createdWithStudents = api.createOrUpdateGroups(List.of(withStudents));
    created.forEach(g -> createdGroupIds.add(g.getId()));
    createdWithStudents.forEach(g -> createdGroupIds.add(g.getId()));

    var students = api.getStudentsByGroupId(createdWithStudents.getFirst().getId(), 1, 10, null);

    assertEquals(2, created.size());
    var createdOne = created.getFirst();
    assertTrue(isValidUUID(createdOne.getId()));
    assertNotNull(createdOne.getCreationDatetime());
    emptyOne.setId(createdOne.getId());
    emptyOne.setCreationDatetime(createdOne.getCreationDatetime());
    assertEquals(createGroupToGroup(emptyOne), createdOne);

    assertEquals(2, students.size());
  }

  @Test
  void manager_write_update_ok() throws ApiException {
    var api = apiAs(managerToken);

    var group =
        api.createOrUpdateGroups(
                List.of(
                    new CreateGroup()
                        .name("name")
                        .ref("ref-" + randomUUID())
                        .creationDatetime(Instant.now())))
            .getFirst();
    createdGroupIds.add(group.getId());

    var toUpdate = List.of(groupToCreateGroup(group).name("A new name zero"));
    var updated = api.createOrUpdateGroups(toUpdate);

    assertTrue(updated.contains(createGroupToGroup(toUpdate.getFirst())));
  }
}
