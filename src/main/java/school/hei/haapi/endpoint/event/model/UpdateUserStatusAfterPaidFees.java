package school.hei.haapi.endpoint.event.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Duration;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
@ToString
@Getter
public class UpdateUserStatusAfterPaidFees extends PojaEvent{

    @JsonProperty("id")
    private String id;

    @JsonProperty("ref")
    private String ref;

    @JsonProperty("status")
    private String status;

    @JsonProperty("email")
    private String email;

    @Override
    public Duration maxConsumerDuration() {
        return Duration.ofSeconds(60);
    }

    @Override
    public Duration maxConsumerBackoffBetweenRetries() {
        return Duration.ofSeconds(60);
    }
}
