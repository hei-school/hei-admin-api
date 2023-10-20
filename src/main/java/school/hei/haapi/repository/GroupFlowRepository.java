package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.AwardedCourse;
import school.hei.haapi.model.GroupFlow;

import java.util.List;

@Repository
public interface GroupFlowRepository extends JpaRepository<GroupFlow, String> {

}
