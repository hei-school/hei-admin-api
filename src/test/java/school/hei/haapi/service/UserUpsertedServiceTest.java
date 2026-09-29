package school.hei.haapi.service;

import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.event.model.UserUpserted;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.event.UserUpsertedService;
import school.hei.haapi.service.sms.SmsContactService;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UsernameExistsException;

class UserUpsertedServiceTest {
  UserUpsertedService userUpsertedService;
  CognitoComponent cognitoComponent;
  UserRepository userRepositoryMock;
  SmsContactService smsContactServiceMock;

  @BeforeEach
  void setUp() {
    cognitoComponent = mock(CognitoComponent.class);
    userRepositoryMock = mock(UserRepository.class);
    smsContactServiceMock = mock(SmsContactService.class);
    userUpsertedService =
        new UserUpsertedService(cognitoComponent, userRepositoryMock, smsContactServiceMock);
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
  void a_cognito_failure_does_not_prevent_the_sms_contact_from_being_created() {
    // e.g. a missing IAM permission on the worker's execution role
    // (CognitoIdentityProviderException) —
    // seen for real in preprod: it must not stop createContactIfMissing from running.
    var user = User.builder().id("u1").build();
    var userUpserted = new UserUpserted().userId("u1").email("a@hei.school");
    when(cognitoComponent.createUser("a@hei.school"))
        .thenThrow(new RuntimeException("cognito-idp:AdminCreateUser not authorized"));
    when(userRepositoryMock.findById("u1")).thenReturn(Optional.of(user));

    userUpsertedService.accept(userUpserted);

    verify(smsContactServiceMock).createContactIfMissing(user);
  }

  @Test
  void also_creates_an_sms_contact_for_the_upserted_user_when_found() {
    var user = User.builder().id("u1").build();
    var userUpserted = new UserUpserted().userId("u1").email("a@hei.school");
    when(userRepositoryMock.findById("u1")).thenReturn(Optional.of(user));

    userUpsertedService.accept(userUpserted);

    verify(smsContactServiceMock).createContactIfMissing(user);
  }

  @Test
  void does_not_try_to_create_a_contact_when_the_user_cannot_be_found() {
    var userUpserted = new UserUpserted().userId("missing").email("a@hei.school");
    when(userRepositoryMock.findById("missing")).thenReturn(Optional.empty());

    userUpsertedService.accept(userUpserted);

    verify(smsContactServiceMock, never()).createContactIfMissing(any());
  }
}
