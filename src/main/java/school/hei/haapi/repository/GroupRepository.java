package school.hei.haapi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Group;

@Repository
public interface GroupRepository extends JpaRepository<Group, String> {
  @Query("select g from Group g")
  Page<Group> getGroups(Pageable pageable);
}
