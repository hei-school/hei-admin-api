package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.haapi.model.StudentResultOverview;

public interface StudentResultOverviewRepository
    extends JpaRepository<StudentResultOverview, String> {}
