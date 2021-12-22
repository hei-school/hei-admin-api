package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.UserRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  public User getById(String userId) {
    return userRepository.getById(userId);
  }

  public User getByEmail(String email) {
    return userRepository.getByEmail(email);
  }

  public List<User> saveAll(List<User> users) {
    return userRepository.saveAll(users);
  }

  public List<User> getByRole(User.Role role) {
    return userRepository.getByRole(role);
  }
}
