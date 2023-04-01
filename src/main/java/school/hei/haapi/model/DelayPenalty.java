package school.hei.haapi.model;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Entity;
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
@NoArgsConstructor
public class DelayPenalty implements Serializable {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private String id;
    private int interestPercent;

    //délai d'intérêt
    private String interestTimerate;

    private int graceDelay;

    private int applicabilityDelayAfterGrace;

    @CreationTimestamp
    private Instant creationDatetime;

}
