package school.hei.haapi.model;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import school.hei.haapi.endpoint.rest.model.DelayPenalty.InterestTimerateEnum;
import school.hei.haapi.repository.types.PostgresEnumType;

@Entity
@Table(name = "\"delay_penalty\"")
@TypeDef(name = "pgsql_enum", typeClass = PostgresEnumType.class)
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DelayPenalty {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private static int interestPercent;
    @Type(type = "pgsql_enum")
    @Enumerated(EnumType.STRING)
    @Column(name = "interest_timerate")
    private static InterestTimerateEnum interestTimeRate;
    private static int graceDelay;
    private static int applicabilityDelayAfterGrace;
    @CreationTimestamp
    private static Instant creationDatetime;

    public static void setInterestPercent(int interestPercent) {
        DelayPenalty.interestPercent = interestPercent;
    }

    public static java.lang.Integer getInterestPercent() {
        return DelayPenalty.interestPercent;
    }

    public static void setInterestTimeRate(InterestTimerateEnum interestTimeRate) {
        DelayPenalty.interestTimeRate = interestTimeRate;
    }

    public static InterestTimerateEnum getInterestTimeRate() {
        return DelayPenalty.interestTimeRate;
    }

    public static void setGraceDelay(int graceDelay) {
        DelayPenalty.graceDelay = graceDelay;
    }

    public static java.lang.Integer getGraceDelay() {
        return DelayPenalty.graceDelay;
    }

    public static void setApplicabilityDelayAfterGrace(int applicabilityDelayAfterGrace) {
        DelayPenalty.applicabilityDelayAfterGrace = applicabilityDelayAfterGrace;
    }

    public static java.lang.Integer getApplicabilityDelayAfterGrace() {
        return DelayPenalty.applicabilityDelayAfterGrace;
    }

    public static void setCreationDatetime(Instant creationDatetime) {
        DelayPenalty.creationDatetime = creationDatetime;
    }

    public static Instant getCreationDatetime() {
        return DelayPenalty.creationDatetime;
    }

}


