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
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class DelayPenalty {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private String id;
  private int interestPercent;
  @Type(type = "pgsql_enum")
  @Enumerated(EnumType.STRING)
  @Column(name = "interest_timerate")
  private InterestTimerateEnum interestTimeRate;
  private int graceDelay;
  private int applicabilityDelayAfterGrace;
  @CreationTimestamp
  private Instant creationDatetime;
}
