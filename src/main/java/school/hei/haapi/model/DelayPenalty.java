package java.school.hei.haapi.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.persistence.*;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "delay_penalty")
public class DelayPenalty {

    @Id
    private String id;

    @Column(name = "interest_percent")
    private BigDecimal interestPercent;

    @Enumerated(EnumType.STRING)
    @Column(name = "interest_timerate")
    private InterestTimeRate interestTimeRate;

    @Column(name = "grace_delay")
    private int graceDelay;

    @Column(name = "applicability_delay_after_grace")
    private int applicabilityDelayAfterGrace;

    @Column(name = "creation_datetime")
    private LocalDateTime creationDatetime;

}

