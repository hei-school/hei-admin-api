package school.hei.haapi.model;

import static java.time.temporal.ChronoUnit.SECONDS;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.IDENTITY;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.Where;
import school.hei.haapi.repository.types.PostgresEnumType;

@Entity
@Table(name = "\"payment\"")
@Getter
@Setter
@TypeDef(name = "pgsql_enum", typeClass = PostgresEnumType.class)
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "update \"payment\" set is_deleted = true where id=?")
@Where(clause = "is_deleted=false")
public class Payment implements Serializable {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @ManyToOne
  @JoinColumn(name = "fee_id", nullable = false)
  private Fee fee;

  @Type(type = "pgsql_enum")
  @Enumerated(STRING)
  private school.hei.haapi.endpoint.rest.model.Payment.TypeEnum type;

  private Integer amount;
  private String comment;

  private Instant creationDatetime;

  private Boolean isDeleted;

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
    return amount == payment.amount
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
