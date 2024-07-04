package school.hei.haapi.model.notEntity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
@ToString
@Getter
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OcsData {
  @JsonProperty("id")
  private String id;

  @JsonProperty("permissions")
  private int permissions;

  @JsonProperty("expiration")
  private String expiration;

  @JsonProperty("name")
  private String name;

  @JsonProperty("url")
  private String url;

  @JsonProperty("path")
  private String path;
}
