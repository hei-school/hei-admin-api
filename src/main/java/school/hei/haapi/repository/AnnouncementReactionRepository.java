package school.hei.haapi.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.AnnouncementReaction;

@Repository
public interface AnnouncementReactionRepository
    extends JpaRepository<AnnouncementReaction, String> {
  Optional<AnnouncementReaction> findByAnnouncement_IdAndUser_Id(
      String announcementId, String userId);

  // TODO: use SQL to compute this operation in a single query
  Integer countByAnnouncement_IdAndReaction(
      String announcementId, AnnouncementReaction.ReactionEnum reaction);
}
