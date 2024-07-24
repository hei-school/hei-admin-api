package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.GroupAttender;

@Repository
public interface GroupAttenderRepository extends JpaRepository<GroupAttender, String> {}
