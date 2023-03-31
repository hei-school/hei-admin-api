package school.hei.haapi.model;

import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import school.hei.haapi.repository.types.PostgresEnumType;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.Instant;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "\"delay_penalty\"")
@TypeDef(name = "pgsql_enum", typeClass = PostgresEnumType.class)
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class DelayPenalty implements Serializable {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private String id;

    @NotBlank
    @Column(name = "interest_percent")
    private Integer interestPercent;

    @Column(name = "interest_timerate")
    @Type(type = "pgsql_enum")
    @Enumerated(EnumType.STRING)
    private InterestTimerateEnum InterestTimeRate;

    @NotBlank
    @Column(name = "grace_delay")
    private Integer graceDelay;

    @NotBlank
    @Column(name = "applicability_delay_after_grace")
    private Integer applicabilityDelayAfterGrace;

    @NotBlank
    @Column(name = "creation_datetime")
    private Instant creationDateTime;

    public enum InterestTimerateEnum {
        DAILY
    }
}
