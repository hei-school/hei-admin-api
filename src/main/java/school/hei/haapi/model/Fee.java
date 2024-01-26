package school.hei.haapi.model;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.IDENTITY;

import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import javax.persistence.Entity;
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
import school.hei.haapi.endpoint.rest.model.FeeStatusEnum;
import school.hei.haapi.endpoint.rest.model.FeeTypeEnum;
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
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User student;

  @Type(type = "pgsql_enum")
  @Enumerated(STRING)
  private FeeStatusEnum status;

  @Type(type = "pgsql_enum")
  @Enumerated(STRING)
  private FeeTypeEnum type;

  private Integer totalAmount;

  private Instant updatedAt;

  private Integer remainingAmount;

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
