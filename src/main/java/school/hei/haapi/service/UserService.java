package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.UserUpserted;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.UserRepository;

import static java.util.stream.Collectors.toUnmodifiableList;

@Service
@AllArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final EventProducer eventProducer;

  public User getById(String userId) {
    return userRepository.getById(userId);
  }

  public User getByEmail(String email) {
    return userRepository.getByEmail(email);
  }

  public List<User> saveAll(List<User> users) {
    List<User> savedUsers = userRepository.saveAll(users);
    eventProducer.produce(users.stream()
        .map(this::toEvent)
        .collect(toUnmodifiableList()));
    return savedUsers;
  }

  private UserUpserted toEvent(User user) {
    return UserUpserted.builder()
        .userId(user.getId())
        .email(user.getEmail())
        .build();
  }

  public List<User> getByRole(User.Role role) {
    return userRepository.getByRole(role);
  }
}
