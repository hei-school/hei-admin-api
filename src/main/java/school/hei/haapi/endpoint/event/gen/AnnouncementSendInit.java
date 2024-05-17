package school.hei.haapi.endpoint.event.gen;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import lombok.*;
import school.hei.haapi.endpoint.rest.model.Scope;
import school.hei.haapi.model.notEntity.Group;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
@ToString
@Data
public class AnnouncementSendInit implements Serializable {
  @JsonProperty("scope")
  private Scope scope;

  @JsonProperty("title")
  private String title;

  @JsonProperty("content")
  private String content;

  @JsonProperty("groups")
  private List<Group> groups;
}
