package school.hei.haapi.model;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
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
    private int interestPercent;

    @Type(type = "pgsql_enum")
    @Enumerated(EnumType.STRING)
    private school.hei.haapi.endpoint.rest.model.DelayPenalty.InterestTimerateEnum interestTimerate;

    private int graceDelay;

    private int applicabilityDelayAfterGrace;

    @CreationTimestamp
    @Getter(AccessLevel.NONE)
    private Instant creationDatetime;
}
