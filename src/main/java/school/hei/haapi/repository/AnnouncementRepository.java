package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Announcement;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, String> {}
