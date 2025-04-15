package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;
import static school.hei.haapi.integration.GroupIT.createGroupToGroup;
import static school.hei.haapi.integration.GroupIT.someCreatableGroup;
import static school.hei.haapi.integration.conf.utils.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.utils.TestUtils.isValidUUID;
import static school.hei.haapi.integration.conf.utils.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.utils.TestUtils.setUpEventBridge;
import static school.hei.haapi.integration.conf.utils.TestUtils.someCreatableStudent;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.TeachingApi;
import school.hei.haapi.endpoint.rest.api.UsersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CreateGroup;
import school.hei.haapi.endpoint.rest.model.Group;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.utils.TestUtils;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

@Testcontainers
@AutoConfigureMockMvc
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
public class DirtyGroupIT extends FacadeITMockedThirdParties {
  @MockBean EventBridgeClient eventBridgeClientMock;

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
    setUpEventBridge(eventBridgeClientMock);
  }

  @Test
  void manager_write_create_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    CreateGroup toCreate3 = someCreatableGroup(new ArrayList<>());
    CreateGroup toCreate4 = someCreatableGroup(new ArrayList<>());
    TeachingApi api = new TeachingApi(manager1Client);

    List<Group> created = api.createOrUpdateGroups(List.of(toCreate3, toCreate4));

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
  }

  @Test
  void manager_write_create_with_student_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(manager1Client);
    UsersApi usersApi = new UsersApi(manager1Client);
    List<Student> studentsList =
        usersApi.createOrUpdateStudents(
            List.of(someCreatableStudent(), someCreatableStudent()), null);

    List<String> studentIds = studentsList.stream().map(Student::getId).toList();
    CreateGroup toCreateWithStudents = someCreatableGroup(studentIds);
    List<Group> createdWithStudent = api.createOrUpdateGroups(List.of(toCreateWithStudents));

    List<Student> students =
        api.getStudentsByGroupId(createdWithStudent.getFirst().getId(), 1, 10, null);

    assertEquals(2, students.size());
  }
}
