package school.hei.haapi.model;

import static javax.persistence.GenerationType.IDENTITY;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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
import school.hei.haapi.repository.types.PostgresEnumType;

@Entity
@Table(name = "\"fee\"")
@Getter
@Setter
@TypeDef(name = "pgsql_enum", typeClass = PostgresEnumType.class)
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Fee implements Serializable {
  private static final Map<school.hei.haapi.endpoint.rest.model.DelayPenalty.InterestTimerateEnum, Integer>
      TIME_RATE_TO_DAYS = Map.of(
      school.hei.haapi.endpoint.rest.model.DelayPenalty.InterestTimerateEnum.DAILY, 1
  );
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;
  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User student;
  @Type(type = "pgsql_enum")
  @Enumerated(EnumType.STRING)
  private school.hei.haapi.endpoint.rest.model.Fee.StatusEnum status;
  @Type(type = "pgsql_enum")
  @Enumerated(EnumType.STRING)
  private school.hei.haapi.endpoint.rest.model.Fee.TypeEnum type;
  private int totalAmount;
  private Instant updatedAt;
  private int remainingAmount;
  private String comment;
  @CreationTimestamp
  @Getter(AccessLevel.NONE)
  private Instant creationDatetime;
  private Instant dueDatetime;
  @OneToMany(mappedBy = "fee")
  private List<Payment> payments;

  public Instant getCreationDatetime() {
    return creationDatetime.truncatedTo(ChronoUnit.MILLIS);
  }

  public int calculateDelayPenalty(DelayPenalty delayPenalty) {
    Instant dueDate = getDueDatetime();
    if (dueDate == null) {
      return remainingAmount;
    }

    Instant now = Instant.now();
    long delay = Duration.between(dueDate, now).toDays();
    if (delay <= 0) {
      return remainingAmount;
    }

    int remainingAmount = getRemainingAmount();
    int totalAmount = getTotalAmount();
    int penaltyAmount = 0;

    if (delay <= delayPenalty.getGraceDelay()) {
      // No penalty during the grace period
      remainingAmount = getRemainingAmount();
    } else if (delay <= (delayPenalty.getGraceDelay() + delayPenalty.getApplicabilityDelayAfterGrace())) {
      penaltyAmount = (int) Math.round(remainingAmount * delayPenalty.getInterestPercent() / 100.0);
    } else {
      int maxPenalty = totalAmount * delayPenalty.getInterestPercent() / 100;
      int appliedDelay = (int) (delay - delayPenalty.getApplicabilityDelayAfterGrace() - delayPenalty.getGraceDelay());
      int applicableMaxPenalty = maxPenalty * appliedDelay / TIME_RATE_TO_DAYS.get(delayPenalty.getInterestTimeRate());

      penaltyAmount = Math.min(applicableMaxPenalty, maxPenalty);
    }

    remainingAmount += penaltyAmount;
    return remainingAmount;
  }
  public int getRemainingAmount() {
    return remainingAmount;
  }

  public void setRemainingAmount(int remainingAmount) {
    this.remainingAmount = remainingAmount;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    Fee fee = (Fee) o;
    return totalAmount == fee.totalAmount
        && remainingAmount == fee.remainingAmount
        && Objects.equals(id, fee.id)
        && Objects.equals(student.getId(), fee.student.getId())
        && status == fee.status
        && type == fee.type
        && Objects.equals(creationDatetime, fee.creationDatetime)
        && Objects.equals(dueDatetime, fee.dueDatetime);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
