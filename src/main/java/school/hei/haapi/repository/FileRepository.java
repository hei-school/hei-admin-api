package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.File;
import school.hei.haapi.model.User;

@Repository
public interface FileRepository extends JpaRepository<File, String> {
  List<File> findAllByUser(User user);

  File getByUserIdAndId(String userId, String id);
}
