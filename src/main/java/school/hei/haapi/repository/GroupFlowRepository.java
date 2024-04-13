package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.GroupFlow;

@Repository
public interface GroupFlowRepository extends JpaRepository<GroupFlow, String> {
  List<GroupFlow> findAllByGroupId(String groupId, Sort sortable);
}
