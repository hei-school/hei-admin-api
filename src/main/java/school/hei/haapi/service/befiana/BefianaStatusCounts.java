package school.hei.haapi.service.befiana;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BefianaStatusCounts {
  private Integer scheduled;
  private Integer processing;
  private Integer accepted;
  private Integer delivered;
  private Integer undeliverable;
  private Integer blocked;
  private Integer failed;
  private Integer unknown;
}
