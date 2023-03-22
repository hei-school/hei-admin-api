package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
  User getByEmail(String email);

  List<User> getByRole(User.Role role, Pageable pageable);

  List<User> findByRoleAndRefContainingIgnoreCaseAndFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(
      User.Role role, String ref, String firstName, String lastName, Pageable pageable);
}
