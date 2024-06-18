package school.hei.haapi.http.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class OrangeDailyTransactionScrappingDetails {
    @JsonProperty("transactionDate")
    private Instant transactionDate;

    private Instant timestamp;

    private List<OrangeTransactionScrappingDetails> transactions;
}
