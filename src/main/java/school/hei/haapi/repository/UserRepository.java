package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
  Optional<User> findByRefContainingIgnoreCase(String ref);
  boolean existsByRefContainingIgnoreCase(String ref);
  User getByEmail(String email);
}
