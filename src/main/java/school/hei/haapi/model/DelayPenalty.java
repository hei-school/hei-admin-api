package school.hei.haapi.model;

import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import school.hei.haapi.repository.types.PostgresEnumType;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "\"delay_penalty\"")
@Getter
@Setter
@TypeDef(name = "pgsql_enum", typeClass = PostgresEnumType.class)
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DelayPenalty implements Serializable {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private String id;

    private int interestPercent;

    @Column(name = "\"interest_timerate\"")
    @Type(type = "pgsql_enum")
    @Enumerated(EnumType.STRING)
    private school.hei.haapi.endpoint.rest.model.DelayPenalty.InterestTimerateEnum interestTimerateEnum;

    private int graceDelay;

    private int applicabilityDelayAfterGrace;

    @CreationTimestamp
    @Getter(AccessLevel.NONE)
    private Instant creationDatetime;

    public Instant getCreationDatetime() {
        return creationDatetime.truncatedTo(ChronoUnit.MILLIS);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        DelayPenalty delayPenalty = (DelayPenalty) o;
        return id != null && Objects.equals(id, delayPenalty.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
