package school.hei.haapi.model;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import school.hei.haapi.repository.types.PostgresEnumType;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "\"delay_penalty\"")
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@TypeDef(name = "pgsql_enum", typeClass = PostgresEnumType.class)
public class DelayPenalty implements Serializable {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private String id;
    private int interest_percent;

    @Type(type = "pgsql_enum")
    @Enumerated(EnumType.STRING)
    private school.hei.haapi.endpoint.rest.model.DelayPenalty.InterestTimerateEnum interest_timerate;

    private int grace_delay;

    private int applicability_delay_after_grace;

    @CreationTimestamp
    private Instant creation_datetime;
}
