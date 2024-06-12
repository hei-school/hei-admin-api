package school.hei.haapi.service;

import static org.springframework.data.domain.Sort.Direction.DESC;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.AnnouncementSendInit;
import school.hei.haapi.endpoint.rest.model.Scope;
import school.hei.haapi.model.Announcement;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.ForbiddenException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.model.notEntity.Group;
import school.hei.haapi.repository.AnnouncementRepository;
import school.hei.haapi.repository.dao.AnnouncementDao;

@Service
@AllArgsConstructor
public class AnnouncementService {

  private final AnnouncementRepository announcementRepository;
  private final AnnouncementDao announcementDao;
  private final EventProducer eventProducer;

  public Announcement findById(String id) {
    return announcementRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Announcement with id #" + id + " not found"));
  }

  public List<Announcement> getAnnouncements(
      Instant from,
      Instant to,
      String authorRef,
      String groupRef,
      PageFromOne page,
      BoundedPageSize pageSize,
      List<Scope> scopes) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "creationDatetime"));
    return announcementDao.findByCriteria(from, to, authorRef, scopes, groupRef, pageable);
  }

  public Announcement postAnnouncement(Announcement announcementToCreate) {
    var saved = announcementRepository.save(announcementToCreate);
    eventProducer.accept(
        List.of(
            AnnouncementSendInit.builder()
                .id(announcementToCreate.getId())
                .title(announcementToCreate.getTitle())
                .scope(announcementToCreate.getScope())
                .senderFullName(
                    announcementToCreate.getAuthor().getFirstName()
                        + " "
                        + announcementToCreate.getAuthor().getLastName())
                .groups(
                    announcementToCreate.getGroups().stream()
                        .map(
                            group ->
                                Group.builder()
                                    .id(group.getId())
                                    .ref(group.getRef())
                                    .name(group.getName())
                                    .build())
                        .toList())
                .build()));
    return saved;
  }

  public Announcement getById(String id, List<Scope> readableScope) {
    Announcement announcement =
        announcementRepository
            .findById(id)
            .orElseThrow(() -> new BadRequestException("Announcement not found"));
    if (!readableScope.contains(announcement.getScope())) {
      throw new ForbiddenException("You are not allowed to check this announcement");
    }
    return announcement;
  }
}
