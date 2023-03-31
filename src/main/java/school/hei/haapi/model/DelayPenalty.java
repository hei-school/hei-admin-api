package school.hei.haapi.model;

import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import school.hei.haapi.endpoint.rest.model.InterestTimerate;
import school.hei.haapi.repository.types.PostgresEnumType;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "\"delay_penalty\"")
@Getter
@Setter
@TypeDef(name = "pgsql_enum", typeClass = PostgresEnumType.class)
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DelayPenalty implements Serializable {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  private int interestPercent;

  @Column(name = "\"interest_timerate\"")
  @Type(type = "pgsql_enum")
  @Enumerated(EnumType.STRING)
  private InterestTimerate interestTimerate;

  private int graceDelay;

  private int applicabilityDelayAfterGrace;

  @CreationTimestamp
  @Getter(AccessLevel.NONE)
  private Instant creationDatetime;

  public Instant getCreationDatetime() {
    return creationDatetime.truncatedTo(ChronoUnit.MILLIS);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    DelayPenalty delayPenalty = (DelayPenalty) o;
    return id != null && Objects.equals(id, delayPenalty.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
