package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.RetakeExamSession;

@Component
public interface RetakeExamSessionRepository extends JpaRepository<RetakeExamSession, String> {}
