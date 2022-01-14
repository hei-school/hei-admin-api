package school.hei.haapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.event.model.gen.UserUpserted;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UsernameExistsException;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserUpsertedServiceTest {
  UserUpsertedService userUpsertedService;
  CognitoComponent cognitoComponent;

  @BeforeEach
  void setUp() {
    cognitoComponent = mock(CognitoComponent.class);
    userUpsertedService = new UserUpsertedService(cognitoComponent);
  }

  @Test
  void newUser_triggers_cognitoUser_creation() {
    String email = "test+" + randomUUID() + "@hei.school";
    UserUpserted userUpserted = new UserUpserted().email(email);
    when(cognitoComponent.createUser(email)).thenReturn(("newCognitoUsername"));

    userUpsertedService.accept(userUpserted);

    verify(cognitoComponent, times(1)).createUser(email);
  }

  @Test
  void existingCognitoUser_is_ignored() {
    String email = "test+" + randomUUID() + "@hei.school";
    UserUpserted userUpserted = new UserUpserted().email(email);
    when(cognitoComponent.createUser(email)).thenThrow((UsernameExistsException.class));

    userUpsertedService.accept(userUpserted); // does not rethrow UsernameExistsException

    verify(cognitoComponent, times(1)).createUser(email);
  }
}