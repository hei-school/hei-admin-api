package school.hei.haapi.service;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.event.model.UserUpserted;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.service.event.UserUpsertedService;
import school.hei.haapi.service.sms.SmsContactService;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UsernameExistsException;

class UserUpsertedServiceTest {
  UserUpsertedService userUpsertedService;
  CognitoComponent cognitoComponent;
  SmsContactService smsContactServiceMock;

  @BeforeEach
  void setUp() {
    cognitoComponent = mock(CognitoComponent.class);
    smsContactServiceMock = mock(SmsContactService.class);
    userUpsertedService = new UserUpsertedService(cognitoComponent, smsContactServiceMock);
  }

  @Test
  void newUser_triggers_cognitoUser_creation() {
    var email = "test+" + randomUUID() + "@hei.school";
    var userUpserted = new UserUpserted().email(email);
    when(cognitoComponent.createUser(email)).thenReturn(("newCognitoUsername"));

    userUpsertedService.accept(userUpserted);

    verify(cognitoComponent, times(1)).createUser(email);
  }

  @Test
  void existingCognitoUser_is_ignored() {
    var email = "test+" + randomUUID() + "@hei.school";
    var userUpserted = new UserUpserted().email(email);
    when(cognitoComponent.createUser(email)).thenThrow((UsernameExistsException.class));

    userUpsertedService.accept(userUpserted); // does not rethrow UsernameExistsException

    verify(cognitoComponent, times(1)).createUser(email);
  }

  @Test
  void a_cognito_failure_does_not_prevent_the_sms_contact_backfill_from_running() {
    // e.g. a missing IAM permission on the worker's execution role
    // (CognitoIdentityProviderException) —
    // seen for real in preprod: it must not stop the backfill from running.
    var userUpserted = new UserUpserted().userId("u1").email("a@hei.school");
    when(cognitoComponent.createUser("a@hei.school"))
        .thenThrow(new RuntimeException("cognito-idp:AdminCreateUser not authorized"));

    userUpsertedService.accept(userUpserted);

    verify(smsContactServiceMock).backfillMissingContacts();
  }

  @Test
  void rechecks_every_enabled_user_not_only_the_upserted_one() {
    var userUpserted = new UserUpserted().userId("u1").email("a@hei.school");

    userUpsertedService.accept(userUpserted);

    verify(smsContactServiceMock, times(1)).backfillMissingContacts();
  }
}
