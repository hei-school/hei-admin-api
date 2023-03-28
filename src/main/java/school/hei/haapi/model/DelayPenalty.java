package school.hei.haapi.model;

import java.time.Instant;
import javax.persistence.Column;
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

import static javax.persistence.GenerationType.IDENTITY;

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
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @Column(nullable = false)
  private int graceDelay;

  @Column(nullable = false)
  private int interestPercent;

  @Column(nullable = false)
  private int applicabilityDelayAfterGrace;

  private Instant creationDatetime;

  @Type(type = "pgsql_enum")
  @Enumerated(EnumType.STRING)
  private InterestTimerate interestTimerate;

  public enum InterestTimerate {
    DAILY
  }
}
