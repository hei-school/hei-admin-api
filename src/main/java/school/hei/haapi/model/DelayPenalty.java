package school.hei.haapi.model;

import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import school.hei.haapi.repository.types.PostgresEnumType;
import school.hei.haapi.endpoint.rest.model.DelayPenalty.InterestTimerateEnum;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "\"create_delay_penalty_change\"")
@Getter
@Setter
@TypeDef(name = "pgsql_enum", typeClass = PostgresEnumType.class)
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class DelayPenalty implements Serializable {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private String id;

    @OneToOne
    @JoinColumn(name = "student_id")
    private User student;

    @Type(type = "pgsql_enum")
    @Enumerated(EnumType.STRING)
    private StatusEnum status;

    private int interestPercent;

    @Type(type = "pgsql_enum")
    @Enumerated(EnumType.STRING)
    private InterestTimerateEnum interestTimerate;

    private int graceDelay;

    private int applicabilityDelayAfterGrace;

    @CreationTimestamp
    @Getter(AccessLevel.NONE)
    private Instant creationDatetime;

    public Instant getCreationDatetime() {
        return creationDatetime.truncatedTo(ChronoUnit.MILLIS);
    }
    public enum StatusEnum {
        GLOBAL, SPECIFIC
    }
}
