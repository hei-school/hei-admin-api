package school.hei.haapi.service.documenso;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DocumensoDocumentEvent {
  @NotNull
  @JsonProperty("id")
  private Long id;
}
