package school.hei.haapi.endpoint.rest.controller;

import static java.util.stream.Collectors.toUnmodifiableList;
import static school.hei.haapi.endpoint.rest.model.Scope.STUDENT;
import static school.hei.haapi.endpoint.rest.model.Scope.TEACHER;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.AnnouncementMapper;
import school.hei.haapi.endpoint.rest.model.Announcement;
import school.hei.haapi.endpoint.rest.model.CreateAnnouncement;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.AnnouncementService;

@AllArgsConstructor
@RestController
public class AnnouncementController {

  private final AnnouncementMapper announcementMapper;
  private final AnnouncementService announcementService;

  @GetMapping("/announcements")
  private List<Announcement> getAnnouncements(
      @RequestParam(name = "page") PageFromOne page,
      @RequestParam(name = "page_size") BoundedPageSize pageSize,
      @RequestParam Instant from,
      @RequestParam Instant to,
      @RequestParam(name = "author_ref") String authorRef) {
    return announcementService
        .getAnnouncements(from, to, authorRef, null, page, pageSize, null)
        .stream()
        .map(announcementMapper::toRest)
        .collect(toUnmodifiableList());
  }

  @PostMapping("/announcements")
  private Announcement postAnnouncement(@RequestBody CreateAnnouncement announcement) {
    return announcementMapper.toRest(announcementMapper.toDomain(announcement));
  }

  @GetMapping("/students/announcements")
  private List<Announcement> getStudentAnnouncements(
      @RequestParam(name = "page") PageFromOne page,
      @RequestParam(name = "page_size") BoundedPageSize pageSize,
      @RequestParam Instant from,
      @RequestParam Instant to,
      @RequestParam(name = "author_ref") String authorRef,
      @RequestParam(name = "group_ref") String groupRef) {

    return announcementService
        .getAnnouncements(from, to, authorRef, groupRef, page, pageSize, STUDENT)
        .stream()
        .map(announcementMapper::toRest)
        .collect(toUnmodifiableList());
  }

  @GetMapping("/teachers/announcements")
  private List<Announcement> getTeacherAnnouncements(
      @RequestParam(name = "page") PageFromOne page,
      @RequestParam(name = "page_size") BoundedPageSize pageSize,
      @RequestParam Instant from,
      @RequestParam Instant to,
      @RequestParam(name = "author_ref") String authorRef) {
    return announcementService
        .getAnnouncements(from, to, authorRef, null, page, pageSize, TEACHER)
        .stream()
        .map(announcementMapper::toRest)
        .collect(toUnmodifiableList());
  }
}
