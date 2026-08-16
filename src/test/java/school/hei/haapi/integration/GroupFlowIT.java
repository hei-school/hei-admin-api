package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.endpoint.rest.model.CreateGroupFlow.MoveTypeEnum.JOIN;
import static school.hei.haapi.endpoint.rest.model.CreateGroupFlow.MoveTypeEnum.LEAVE;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestAuth.tokenFor;
import static school.hei.haapi.integration.conf.TestMocks.setUpEventBridge;
import static school.hei.haapi.integration.conf.TestMocks.setUpS3Service;
import static school.hei.haapi.integration.testData.GroupTestData.createGroupFlow;
import static school.hei.haapi.integration.testData.GroupTestData.g1;
import static school.hei.haapi.integration.testData.GroupTestData.g2;
import static school.hei.haapi.integration.testData.ManagerTestData.hasina;
import static school.hei.haapi.integration.testData.StudentTestData.axel;
import static school.hei.haapi.integration.testData.StudentTestData.freddy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.rest.api.GroupsApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CreateGroupFlow;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.GroupFlowRepository;
import school.hei.haapi.repository.GroupRepository;
import school.hei.haapi.repository.UserRepository;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

public class GroupFlowIT extends FacadeITMockedThirdParties {
  @MockBean private EventBridgeClient eventBridgeClientMock;
  @Autowired private UserRepository userRepository;
  @Autowired private GroupRepository groupRepository;
  @Autowired private GroupFlowRepository groupFlowRepository;

  private User studentAxel;
  private User studentFreddy;
  private User managerHasina;
  private Group groupOne;
  private Group groupTwo;
  private GroupFlow axelJoinsGroupOne;
  private GroupFlow freddyJoinsGroupOne;

  private String managerToken;

  private void setUpTestData() {
    studentAxel = userRepository.save(axel());
    studentFreddy = userRepository.save(freddy());
    managerHasina = userRepository.save(hasina());

    groupOne = groupRepository.save(g1());
    groupTwo = groupRepository.save(g2());

    axelJoinsGroupOne = groupFlowRepository.save(createGroupFlow(studentAxel, groupOne));
    freddyJoinsGroupOne = groupFlowRepository.save(createGroupFlow(studentFreddy, groupOne));
  }

  @BeforeEach
  public void setUp() {
    setUpEventBridge(eventBridgeClientMock);
    setUpTestData();
    setUpS3Service(fileService, studentAxel);

    managerToken = tokenFor(casdoorAuthServiceMock, managerHasina);
  }

  @AfterEach
  void tearDown() {
    // the tests add flows of their own, so sweep everything the two groups carry
    groupFlowRepository.deleteAll(
        groupFlowRepository.findAll().stream()
            .filter(f -> ownedGroupIds().contains(f.getGroup().getId()))
            .toList());
    groupRepository.deleteAll(List.of(groupOne, groupTwo));
    userRepository.deleteAll(List.of(studentAxel, studentFreddy, managerHasina));
  }

  private List<String> ownedGroupIds() {
    return new ArrayList<>(List.of(groupOne.getId(), groupTwo.getId()));
  }

  private GroupsApi apiAs(String token) {
    return new GroupsApi(anApiClient(token));
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  private CreateGroupFlow axelLeavesGroupOne() {
    return new CreateGroupFlow()
        .groupId(groupOne.getId())
        .studentId(studentAxel.getId())
        .moveType(LEAVE);
  }

  private CreateGroupFlow freddyJoinsGroupTwo() {
    return new CreateGroupFlow()
        .groupId(groupTwo.getId())
        .studentId(studentFreddy.getId())
        .moveType(JOIN);
  }

  private CreateGroupFlow freddyLeavesGroupOne() {
    return new CreateGroupFlow()
        .groupId(groupOne.getId())
        .studentId(studentFreddy.getId())
        .moveType(LEAVE);
  }

  @Test
  void manager_read_grouped_students_ok() throws ApiException {
    var groupOneStudents = apiAs(managerToken).getStudentsByGroupId(groupOne.getId(), 1, 10, null);

    assertFalse(groupOneStudents.isEmpty());
    assertTrue(groupOneStudents.stream().anyMatch(s -> studentAxel.getId().equals(s.getId())));
  }

  @Test
  void student_leaves_same_group_ko() {
    var api = apiAs(managerToken);
    var expectedBody =
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Student has already left this group\"}";

    assertThrowsApiException(
        expectedBody,
        () -> {
          api.moveOrDeleteStudentInGroup(studentAxel.getId(), List.of(axelLeavesGroupOne()));
          api.moveOrDeleteStudentInGroup(studentAxel.getId(), List.of(axelLeavesGroupOne()));
        });
  }

  @Test
  void insert_two_student_in_same_group_ko() throws ApiException {
    var api = apiAs(managerToken);
    var expectedBody = "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Student is already in group\"}";

    var groupTwoStudents = api.getStudentsByGroupId(groupTwo.getId(), 1, 20, null);
    assertFalse(groupTwoStudents.stream().anyMatch(s -> studentFreddy.getId().equals(s.getId())));

    assertThrowsApiException(
        expectedBody,
        () -> {
          api.moveOrDeleteStudentInGroup(studentFreddy.getId(), List.of(freddyJoinsGroupTwo()));
          api.moveOrDeleteStudentInGroup(studentFreddy.getId(), List.of(freddyJoinsGroupTwo()));
        });
  }

  @Test
  void manager_moves_a_student_to_another_group_ok() throws ApiException {
    var api = apiAs(managerToken);

    api.moveOrDeleteStudentInGroup(
        studentFreddy.getId(), List.of(freddyLeavesGroupOne(), freddyJoinsGroupTwo()));

    var groupOneStudents = api.getStudentsByGroupId(groupOne.getId(), 1, 10, null);
    var groupTwoStudents = api.getStudentsByGroupId(groupTwo.getId(), 1, 10, null);

    assertEquals(1, groupOneStudents.size());
    assertEquals(studentAxel.getId(), groupOneStudents.getFirst().getId());
    assertEquals(1, groupTwoStudents.size());
    assertEquals(studentFreddy.getId(), groupTwoStudents.getFirst().getId());
  }
}
