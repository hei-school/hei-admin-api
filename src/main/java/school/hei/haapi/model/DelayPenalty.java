package school.hei.haapi.model;

import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Builder;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import school.hei.haapi.repository.types.PostgresEnumType;

@AllArgsConstructor
@NoArgsConstructor
@Table(name = "\"delay_penalty\"")
@TypeDef(name = "pgsql_enum", typeClass = PostgresEnumType.class)
@Entity
@Builder
@Getter
@Setter
public class DelayPenalty {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private String id;
  private Instant creationDatetime;
  private Integer interestPercent;
  @Type(type = "pgsql_enum")
  @Enumerated(EnumType.STRING)
  private school.hei.haapi.endpoint.rest.model.DelayPenalty.InterestTimerateEnum interestTimerate;
  private Integer graceDelay;
  private Integer applicabilityDelayAfterGrace;
}
