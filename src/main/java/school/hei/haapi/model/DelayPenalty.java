package school.hei.haapi.model;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DelayPenalty {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    private int interestPercent ;

    private String interestTimeRate;

    private int graceDelay;

    private int applicabilityDelayAfterGrace;

    private Date creationDatetime;


} ;
