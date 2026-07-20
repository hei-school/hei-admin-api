package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.RetakeExamSession;

@Repository
public interface RetakeExamSessionRepository extends JpaRepository<RetakeExamSession, String> {}
