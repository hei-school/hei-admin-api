package school.hei.haapi.integration;

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
import school.hei.haapi.integration.conf.ClientUtils;

import static java.time.Instant.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = GroupIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class GroupIT {

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = 8082;

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }

  private static ApiClient aClient(String token) {
    return ClientUtils.aClient(token, ContextInitializer.SERVER_PORT);
  }

  @MockBean
  private CognitoComponent cognitoComponent;

  @Test
  void student_can_get_group1() throws ApiException {
    ApiClient student1Client = aClient(ClientUtils.STUDENT1_TOKEN);
    when(cognitoComponent.findEmailByBearer(ClientUtils.STUDENT1_TOKEN))
        .thenReturn("ryan@hei.school");

    GroupsApi api = new GroupsApi(student1Client);
    Group group = api.findGroupById("TODO:school1", ClientUtils.GROUP1_ID);

    assertEquals("Name of G1", group.getName());
    assertEquals("G1", group.getRef());
    assertTrue(group.getCreationDatetime().isBefore(now()));
  }
}
