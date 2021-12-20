package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Group;

@Repository
public interface GroupRepository extends JpaRepository<Group, String> {
}
