package school.hei.haapi.model;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "/delay_penalty")
@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DelayPenalty {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    private int interestPercent;
    @Type(type = "pgsql_enum")
    @Enumerated(EnumType.STRING)
    private String interestTimeRate;

    private int graceDelay;

    private int applicabilityDelayAfterGrace;
    @CreationTimestamp
    private Date creationDatetime;
} ;
