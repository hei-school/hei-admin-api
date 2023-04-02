package school.hei.haapi.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import school.hei.haapi.repository.types.PostgresEnumType;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "\"delay_penalty\"")
@Getter
@Setter
@TypeDef(name = "pgsql_enum", typeClass = PostgresEnumType.class)
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class DelayPenalty implements Serializable {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private String id;
    @Type(type = "pgsql_enum")
    @Enumerated(EnumType.STRING)
    private school.hei.haapi.endpoint.rest.model.DelayPenalty.InterestTimeRateEnum interestTimeRate;
    private int interestPercent;
    private int graceDelay;
    private int applicabilityDelayAfterGrace;
    @CreationTimestamp
    @Getter(AccessLevel.NONE)
    private Instant creationDatetime;

    public Instant getCreationDatetime() {
        return creationDatetime.truncatedTo(ChronoUnit.MILLIS);
    }
}
