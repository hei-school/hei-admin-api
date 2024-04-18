package school.hei.haapi.endpoint.rest.controller;

import static java.util.stream.Collectors.toUnmodifiableList;
import static school.hei.haapi.endpoint.rest.model.Scope.GLOBAL;
import static school.hei.haapi.endpoint.rest.model.Scope.MANAGER;
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
import school.hei.haapi.endpoint.rest.model.Scope;
import school.hei.haapi.endpoint.rest.validator.AnnouncementValidator;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.AnnouncementService;

@AllArgsConstructor
@RestController
public class AnnouncementController {

  public static final List<Scope> STUDENT_READABLE_SCOPES = List.of(GLOBAL, STUDENT);
  public static final List<Scope> TEACHER_READABLE_SCOPES = List.of(GLOBAL, STUDENT, TEACHER);
  public static final List<Scope> MANAGER_READABLE_SCOPES =
      List.of(GLOBAL, STUDENT, TEACHER, MANAGER);
  private final AnnouncementMapper announcementMapper;
  private final AnnouncementService announcementService;
  private final AnnouncementValidator announcementValidator;

  @GetMapping("/announcements")
  private List<Announcement> getAnnouncements(
      @RequestParam(name = "page") PageFromOne page,
      @RequestParam(name = "page_size") BoundedPageSize pageSize,
      @RequestParam(required = false) Instant from,
      @RequestParam(required = false) Instant to,
      @RequestParam(name = "author_ref", required = false) String authorRef,
      @RequestParam(name = "scope", required = false) Scope scope) {
    return announcementService
        .getAnnouncements(
            from,
            to,
            authorRef,
            null,
            page,
            pageSize,
            scope == null ? MANAGER_READABLE_SCOPES : List.of(scope))
        .stream()
        .map(announcementMapper::toRest)
        .collect(toUnmodifiableList());
  }

  @PostMapping("/announcements")
  private Announcement postAnnouncement(@RequestBody CreateAnnouncement announcement) {
    announcementValidator.accept(announcement);
    return announcementMapper.toRest(
        announcementService.postAnnouncement(announcementMapper.toDomain(announcement)));
  }

  @GetMapping("/students/announcements")
  private List<Announcement> getStudentAnnouncements(
      @RequestParam(name = "page") PageFromOne page,
      @RequestParam(name = "page_size") BoundedPageSize pageSize,
      @RequestParam(required = false) Instant from,
      @RequestParam(required = false) Instant to,
      @RequestParam(name = "author_ref", required = false) String authorRef,
      @RequestParam(name = "group_ref", required = false) String groupRef) {

    return announcementService
        .getAnnouncements(from, to, authorRef, groupRef, page, pageSize, STUDENT_READABLE_SCOPES)
        .stream()
        .map(announcementMapper::toRest)
        .collect(toUnmodifiableList());
  }

  @GetMapping("/teachers/announcements")
  private List<Announcement> getTeacherAnnouncements(
      @RequestParam(name = "page") PageFromOne page,
      @RequestParam(name = "page_size") BoundedPageSize pageSize,
      @RequestParam(required = false) Instant from,
      @RequestParam(required = false) Instant to,
      @RequestParam(name = "author_ref", required = false) String authorRef) {
    return announcementService
        .getAnnouncements(from, to, authorRef, null, page, pageSize, TEACHER_READABLE_SCOPES)
        .stream()
        .map(announcementMapper::toRest)
        .collect(toUnmodifiableList());
  }
}
