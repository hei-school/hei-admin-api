package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.haapi.model.FeeCreationTask;

public interface FeeCreationTaskRepository extends JpaRepository<FeeCreationTask, String> {
  List<FeeCreationTask> findAllByJobId(String jobId);
}
