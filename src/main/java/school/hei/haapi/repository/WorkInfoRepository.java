package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.User;
import school.hei.haapi.model.WorkInfo;

@Repository
public interface WorkInfoRepository extends JpaRepository<WorkInfo, String> {
  List<WorkInfo> findWorkInfosByStudent(User student);
}
