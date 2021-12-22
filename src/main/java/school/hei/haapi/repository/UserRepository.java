package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.User;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
  User getByEmail(String email);

  List<User> getByRole(User.Role role);
}
