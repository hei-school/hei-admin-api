package school.hei.haapi.integration.testData;

import static java.util.UUID.randomUUID;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import school.hei.haapi.endpoint.rest.model.Scope;
import school.hei.haapi.model.Announcement;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.User;

public class AnnouncementTestData {
  public static Announcement anAnnouncement(User author, Scope scope, String title) {
    return anAnnouncement(author, scope, title, List.of());
  }

  public static Announcement anAnnouncement(
      User author, Scope scope, String title, List<Group> targetGroups) {
    return Announcement.builder()
        .id(randomUUID().toString())
        .author(author)
        .scope(scope)
        .title(title)
        .content(title + " — content")
        .groups(new ArrayList<>(targetGroups))
        .reactions(new ArrayList<>())
        .build();
  }

  /**
   * {@code creationDatetime} is a {@code @CreationTimestamp}: it can only be forced by writing the
   * row a second time, which is what the date-filter assertions need.
   */
  public static Announcement withCreationDatetime(Announcement announcement, Instant datetime) {
    announcement.setCreationDatetime(datetime);
    return announcement;
  }
}
