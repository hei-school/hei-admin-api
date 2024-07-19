package school.hei.haapi.model.notEntity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
@ToString
@Data
public class Group implements Serializable {
  @JsonProperty("id")
  private String id;

  @JsonProperty("ref")
  private String ref;

  @JsonProperty("name")
  private String name;
}
