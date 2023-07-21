package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.haapi.model.Version;

public interface TranscriptVersionRepository extends JpaRepository<Version,String> {
}
