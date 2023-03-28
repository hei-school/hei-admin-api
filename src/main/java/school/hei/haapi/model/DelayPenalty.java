package school.hei.haapi.model;

import static javax.persistence.GenerationType.IDENTITY;
import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import school.hei.haapi.repository.types.PostgresEnumType;

@Entity
@Table(name = "\"delay_penalty\"")
@Getter
@Setter
@TypeDef(name = "pgsql_enum", typeClass = PostgresEnumType.class)
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DelayPenalty {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;
  @Type(type = "pgsql_enum")
  @Enumerated(EnumType.STRING)
  private school.hei.haapi.endpoint.rest.model.DelayPenalty.InterestTimerateEnum interestTimeRate;
  private int interestPercent;
  private int graceDelay;
  private int applicabilityDelayAfterGrace;
  private Instant creationDatetime;
  private Instant lastUpdateDate;
}