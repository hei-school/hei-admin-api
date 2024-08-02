package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.GroupAttender;

@Repository
public interface GroupAttenderRepository extends JpaRepository<GroupAttender, String> {
  void deleteByStudentIdAndGroupId(String studentId, String groupId);

  List<GroupAttender> findAllByGroupId(String groupId);

  List<GroupAttender> findAllByStudentId(String studentId);
}
