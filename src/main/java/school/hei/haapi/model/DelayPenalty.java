package school.hei.haapi.model;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
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
    private String id;

    @Column(nullable = false)
    private int interestPercent;

    @Type(type = "pgsql_enum")
    @Enumerated(EnumType.STRING)
    private school.hei.haapi.endpoint.rest.model.DelayPenalty.InterestTimerateEnum interestTimerate;

    @Column(nullable = false)
    private int graceDelay;

    @Column(nullable = false)
    private int applicabilityDelayAfterGrace;

    @CreationTimestamp
    private Instant creationDatetime;
}
