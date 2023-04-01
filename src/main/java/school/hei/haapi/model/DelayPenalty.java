package school.hei.haapi.model;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.springframework.lang.NonNull;
import org.springframework.lang.NonNullApi;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "\"delay_penalty\"")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@EqualsAndHashCode
@NoArgsConstructor
public class DelayPenalty implements Serializable {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @NotNull(message = "must not be null")
    private String id;

    @Column(nullable = false)
    @NotEmpty
    @NotNull(message = "must not be null")
    private int interestPercent;

    @Type(type = "pgsql_enum")
    @Enumerated(EnumType.STRING)
    @NotEmpty
    @NotNull(message = "must not be null")
    private school.hei.haapi.endpoint.rest.model.DelayPenalty.InterestTimerateEnum interestTimerate;

    @Column(nullable = false)
    @NotEmpty
    @NotNull(message = "must not be null")
    private int graceDelay;

    @Column(nullable = false)
    @NotNull(message = "must not be null")
    private int applicabilityDelayAfterGrace;

    @CreationTimestamp
    @NotNull(message = "must not be null")
    @NotEmpty
    private Instant creationDatetime;
}
