package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.endpoint.rest.model.CreateGroupFlow.MoveTypeEnum.JOIN;
import static school.hei.haapi.endpoint.rest.model.CreateGroupFlow.MoveTypeEnum.LEAVE;
import static school.hei.haapi.integration.GroupFlowIT.ContextInitializer.SERVER_PORT;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.StudentIT.student2;
import static school.hei.haapi.integration.conf.TestUtils.GROUP1_ID;
import static school.hei.haapi.integration.conf.TestUtils.GROUP2_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT2_ID;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpEventBridge;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.TeachingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CreateGroupFlow;
import school.hei.haapi.endpoint.rest.model.GroupFlow;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.MockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = GroupFlowIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class GroupFlowIT extends MockedThirdParties {
  @MockBean private EventBridgeClient eventBridgeClientMock;

  @BeforeEach
  public void setUp() {
    setUpCognito(cognitoComponentMock);
    setUpEventBridge(eventBridgeClientMock);
    setUpS3Service(fileService, student1());
  }

  @Test
  void manager_read_grouped_students_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(manager1Client);

    List<Student> group1Students = api.getStudentsByGroupId(GROUP1_ID);

    assertEquals(2, group1Students.size());
  }

  @Test
  @DirtiesContext
  void manager_moves_student2_to_group2_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(manager1Client);

    List<GroupFlow> student2move =
        api.moveOrDeleteStudentInGroup(
            STUDENT2_ID, List.of(createStudent2LeavesGroup1(), createStudent2JoinGroup2()));
    List<Student> group1Students = api.getStudentsByGroupId(GROUP1_ID);
    List<Student> group2Students = api.getStudentsByGroupId(GROUP2_ID);

    assertEquals(1, group1Students.size());
    assertEquals(2, group2Students.size());
    assertTrue(group2Students.containsAll(List.of(student2(), student1())));
  }

  public GroupFlow student2GroupFlow() {
    return new GroupFlow()
        .groupId(GROUP1_ID)
        .studentId(STUDENT2_ID)
        .moveType(GroupFlow.MoveTypeEnum.LEAVE);
  }

  public CreateGroupFlow createStudent2JoinGroup2() {
    return new CreateGroupFlow().groupId(GROUP2_ID).studentId(STUDENT2_ID).moveType(JOIN);
  }

  public CreateGroupFlow createStudent2LeavesGroup1() {
    return new CreateGroupFlow().groupId(GROUP1_ID).studentId(STUDENT2_ID).moveType(LEAVE);
  }

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, SERVER_PORT);
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
