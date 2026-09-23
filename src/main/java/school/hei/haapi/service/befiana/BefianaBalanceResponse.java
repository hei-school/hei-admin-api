package school.hei.haapi.service.befiana;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BefianaBalanceResponse {
  @JsonAlias({"availableBalance", "available_balance", "balance", "credit", "solde"})
  private Integer availableBalance;
}
