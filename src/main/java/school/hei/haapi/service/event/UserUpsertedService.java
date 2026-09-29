package school.hei.haapi.service.event;

import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.model.UserUpserted;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.sms.SmsContactService;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UsernameExistsException;

@Service
@AllArgsConstructor
@Slf4j
public class UserUpsertedService implements Consumer<UserUpserted> {

  private final CognitoComponent cognitoComponent;
  private final UserRepository userRepository;
  private final SmsContactService smsContactService;

  @Override
  public void accept(UserUpserted userUpserted) {
    createCognitoUser(userUpserted.getEmail());

    userRepository
        .findById(userUpserted.getUserId())
        .ifPresent(smsContactService::createContactIfMissing);
  }

  private void createCognitoUser(String email) {
    try {
      cognitoComponent.createUser(email);
    } catch (UsernameExistsException e) {
      log.info("User already exists, do nothing: email={}", email);
    } catch (RuntimeException e) {
      log.error("Failed to create Cognito user for email={}", email, e);
    }
  }
}
