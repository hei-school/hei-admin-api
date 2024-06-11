package school.hei.haapi.endpoint.event.gen;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import javax.annotation.processing.Generated;
import lombok.*;
import school.hei.haapi.endpoint.rest.model.Scope;
import school.hei.haapi.model.notEntity.Group;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
@ToString
@Data
@Generated("EventBridge")
public class AnnouncementSendInit implements Serializable {
  @JsonProperty("id")
  private String id;

  @JsonProperty("scope")
  private Scope scope;

  @JsonProperty("title")
  private String title;

  @JsonProperty("senderFullName")
  private String senderFullName;

  @JsonProperty("groups")
  private List<Group> groups;
}
