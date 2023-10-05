package school.hei.haapi.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.AwardedCourse;

@Repository
public interface AwardedCourseRepository extends JpaRepository<AwardedCourse, String> {

  AwardedCourse getByIdAndGroupId(String Id, String groupId);

  List<AwardedCourse> findByGroupId(String groupId, Pageable pageable);
}
