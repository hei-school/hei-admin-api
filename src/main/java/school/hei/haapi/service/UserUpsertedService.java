package school.hei.haapi.service;

import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.model.gen.UserUpserted;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.model.exception.NotImplementedException;

@Service
@AllArgsConstructor
public class UserUpsertedService implements Consumer<UserUpserted> {

  private final CognitoComponent cognitoComponent;

  @Override
  public void accept(UserUpserted userUpserted) {
    try {
      cognitoComponent.createUser(userUpserted.getEmail());
    } catch (Exception e) {
      throw new NotImplementedException("Ignore duplication");
    }
  }
}
