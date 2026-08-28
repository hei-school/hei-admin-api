package school.hei.haapi.service.documenso;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DocumensoWebhookPayload {
  @NotNull
  @JsonProperty("event")
  private String event;

  @JsonProperty("payload")
  private DocumensoDocumentEvent payload;

  public boolean isDocumentCompleted() {
    return event != null && event.contains("COMPLETED");
  }
}
