package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.UserRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {

  private final UserRepository repository;

  public User getById(String userId) {
    return repository.getById(userId);
  }

  public User findByEmail(String email) {
    return repository.findByEmail(email);
  }
}
