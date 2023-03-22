package school.hei.haapi.service;

import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.model.gen.UserUpserted;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UsernameExistsException;

@Service
@AllArgsConstructor
@Slf4j
public class UserUpsertedService implements Consumer<UserUpserted> {

  private final CognitoComponent cognitoComponent;

  @Override
  public void accept(UserUpserted userUpserted) {
    String email = userUpserted.getEmail();
    try {
      cognitoComponent.createUser(email);
    } catch (UsernameExistsException e) {
      log.info("User already exists, do nothing: email={}", email);
    }
  }
}
