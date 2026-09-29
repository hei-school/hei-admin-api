package school.hei.haapi.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.haapi.model.DocumensoFolder;

public interface DocumensoFolderRepository extends JpaRepository<DocumensoFolder, String> {
  Optional<DocumensoFolder> findByPath(String path);
}
