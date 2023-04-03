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
public class DelayPenaltyChange {
    private BigDecimal interestPercent;
    private school.hei.haapi.model.InterestTimeRate interestTimeRate;
    private int graceDelay;
    private int applicabilityDelayAfterGrace;
}