package school.hei.haapi.endpoint.event.model;

import static java.util.UUID.randomUUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import school.hei.haapi.endpoint.rest.model.Scope;
import school.hei.haapi.model.User;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
@ToString
@Data
public class AnnouncementEmailSendRequested extends PojaEvent {
  @JsonProperty("id")
  private String id;

  @JsonProperty("announcement_id")
  private String announcementId;

  @JsonProperty("scope")
  private Scope scope;

  @JsonProperty("title")
  private String title;

  @JsonProperty("senderFullName")
  private String senderFullName;

  private MailUser to;

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofMinutes(2);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofMinutes(1);
  }

  @Builder
  public record MailUser(String id, String email) {
    public static MailUser from(User user) {
      return new MailUser(user.getId(), user.getEmail());
    }
  }

  public static AnnouncementEmailSendRequested from(
      AnnouncementSendInit announcementSendInit, MailUser mailUser) {
    return AnnouncementEmailSendRequested.builder()
        .id(randomUUID().toString())
        .announcementId(announcementSendInit.getId())
        .scope(announcementSendInit.getScope())
        .senderFullName(announcementSendInit.getSenderFullName())
        .title(announcementSendInit.getTitle())
        .to(mailUser)
        .build();
  }
}
