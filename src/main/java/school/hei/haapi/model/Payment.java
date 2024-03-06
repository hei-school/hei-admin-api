package school.hei.haapi.model;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "\"payment\"")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "update \"payment\" set is_deleted = true where id = ?")
@Where(clause = "is_deleted = false")
public class Payment implements Serializable {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @ManyToOne
  @JoinColumn(name = "fee_id", nullable = false)
  private Fee fee;

  @JdbcTypeCode(NAMED_ENUM)
  @Enumerated(STRING)
  private school.hei.haapi.endpoint.rest.model.Payment.TypeEnum type;

  private Integer amount;
  private String comment;

  private Instant creationDatetime;

  private boolean isDeleted;

  public Instant getCreationDatetime() {
    return creationDatetime.truncatedTo(SECONDS);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    Payment payment = (Payment) o;
    return Objects.equals(amount, payment.amount)
        && Objects.equals(id, payment.id)
        && Objects.equals(fee.getId(), payment.getFee().getId())
        && type == payment.type
        && getCreationDatetime().compareTo(payment.getCreationDatetime()) == 0;
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
