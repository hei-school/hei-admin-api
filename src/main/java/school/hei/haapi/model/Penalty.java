package school.hei.haapi.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.Instant;

import static javax.persistence.GenerationType.IDENTITY;
@Entity
@Table(name = "\"penalty\"")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Penalty implements Serializable {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private String id;

    private Integer interestPercent;

    private TimeRate interestTimeRate;

    private Integer graceDelay;

    private Integer applicabilityDelayAfterGrace;

    private Instant creationDateTime;

    public enum TimeRate {
        DAILY
    }
}
