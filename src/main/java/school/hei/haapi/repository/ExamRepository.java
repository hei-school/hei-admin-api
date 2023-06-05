package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Exam;

@Repository
public interface ExamRepository extends JpaRepository<Exam, String> {

  List<Exam> findByCourseId(String courseId);
}
