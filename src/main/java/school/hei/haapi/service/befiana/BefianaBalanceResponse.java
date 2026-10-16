package school.hei.haapi.service.befiana;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BefianaBalanceResponse {
  private Integer available;
  private Integer reserved;
  private Integer total;

  @com.fasterxml.jackson.annotation.JsonProperty("valid_until")
  private LocalDate validUntil;
}
