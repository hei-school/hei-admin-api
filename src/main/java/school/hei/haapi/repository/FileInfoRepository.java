package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.FileInfo;

@Repository
public interface FileInfoRepository extends JpaRepository<FileInfo, String> {
  FileInfo getByUserIdAndId(String userId, String id);

  List<FileInfo> findAllByUserIsNull();

  FileInfo findByUserIsNullAndId(String id);
}
