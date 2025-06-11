package school.hei.haapi.model;

import static jakarta.persistence.CascadeType.REMOVE;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;
import static school.hei.haapi.endpoint.rest.model.FeeCategory.UNKNOWN;
import static school.hei.haapi.model.fee.PaymentType.BANK;
import static school.hei.haapi.model.fee.PaymentType.MPBS;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import school.hei.haapi.endpoint.rest.model.FeeCategory;
import school.hei.haapi.endpoint.rest.model.FeeFrequency;
import school.hei.haapi.endpoint.rest.model.FeeStatusEnum;
import school.hei.haapi.endpoint.rest.model.FeeTypeEnum;
import school.hei.haapi.model.Mpbs.Mpbs;
import school.hei.haapi.model.fee.PaymentType;

@Entity
@Table(name = "\"fee\"")
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "update \"fee\" set is_deleted = true where id = ?")
@Where(clause = "is_deleted = false")
@EqualsAndHashCode
public class Fee implements Serializable {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User student;

  @JdbcTypeCode(NAMED_ENUM)
  @Enumerated(STRING)
  private FeeStatusEnum status;

  @JdbcTypeCode(NAMED_ENUM)
  @Enumerated(STRING)
  private FeeTypeEnum type;

  private Integer totalAmount;

  @EqualsAndHashCode.Exclude private Instant updatedAt;

  private Integer remainingAmount;

  @EqualsAndHashCode.Exclude private String comment;

  @EqualsAndHashCode.Exclude private boolean isDeleted;

  @CreationTimestamp
  @Getter(AccessLevel.NONE)
  private Instant creationDatetime;

  private Instant dueDatetime;

  @OneToMany(mappedBy = "fee", cascade = REMOVE)
  @JsonIgnore
  @EqualsAndHashCode.Exclude
  private List<Payment> payments;

  @OneToMany(mappedBy = "fee", cascade = REMOVE)
  @EqualsAndHashCode.Exclude
  private List<Mpbs> mobilePayments;

  @JdbcTypeCode(NAMED_ENUM)
  @Enumerated(STRING)
  private FeeCategory category = UNKNOWN;

  @JdbcTypeCode(NAMED_ENUM)
  @Enumerated(STRING)
  private FeeFrequency frequency;

  public Instant getCreationDatetime() {
    return creationDatetime.truncatedTo(ChronoUnit.MILLIS);
  }

  public Fee(Fee fee) {
    this.id = fee.getId();
    this.student = fee.getStudent();
    this.status = fee.getStatus();
    this.type = fee.getType();
    this.totalAmount = fee.getTotalAmount();
    this.remainingAmount = fee.getRemainingAmount();
    this.comment = fee.getComment();
    this.category = fee.getCategory();
    this.frequency = fee.getFrequency();
    this.mobilePayments = fee.getMobilePayments();
    this.creationDatetime = fee.getCreationDatetime();
    this.dueDatetime = fee.getDueDatetime();
    this.payments = fee.getPayments();
    this.isDeleted = fee.isDeleted();
    this.updatedAt = fee.getUpdatedAt();
  }

  public String describe() {
    return """
Fee : {"id" : "%s", "remainingAmount" : "%s", "totalAmount" : "%s", "dueDatetime" : "%s", "actualStatus" : "%s"}
"""
        .formatted(getId(), getRemainingAmount(), getTotalAmount(), getDueDatetime(), getStatus());
  }

  @Override
  public String toString() {
    return "Fee{"
        + "id='"
        + id
        + '\''
        + ", status="
        + status
        + ", type="
        + type
        + ", totalAmount="
        + totalAmount
        + ", updatedAt="
        + updatedAt
        + ", remainingAmount="
        + remainingAmount
        + ", comment='"
        + comment
        + '\''
        + ", isDeleted="
        + isDeleted
        + ", creationDatetime="
        + creationDatetime
        + ", dueDatetime="
        + dueDatetime
        + '}';
  }

  public PaymentType getPaymentType() {
    if (!this.getMobilePayments().isEmpty()) {
      return MPBS;
    } else {
      return BANK;
    }
  }
}
