package school.hei.haapi.model;

import static jakarta.persistence.CascadeType.REMOVE;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.GenerationType.IDENTITY;
import static java.util.Comparator.comparing;
import static java.util.function.Predicate.isEqual;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;
import static school.hei.haapi.endpoint.rest.model.FeeCategory.UNKNOWN;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.PAID;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.PENDING;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.UNPAID;
import static school.hei.haapi.model.fee.PaymentType.BANK;
import static school.hei.haapi.model.fee.PaymentType.MPBS;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import school.hei.haapi.endpoint.rest.model.FeeCategory;
import school.hei.haapi.endpoint.rest.model.FeeFrequency;
import school.hei.haapi.endpoint.rest.model.FeeStatusEnum;
import school.hei.haapi.endpoint.rest.model.FeeTypeEnum;
import school.hei.haapi.endpoint.rest.model.MpbsStatus;
import school.hei.haapi.model.fee.PaymentType;
import school.hei.haapi.model.mpbs.Mpbs;

@Entity
@Table(name = "\"fee\"")
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "update \"fee\" set is_deleted = true where id = ?")
@FilterDef(name = "undeletedFees", defaultCondition = "is_deleted = false")
@Filter(name = "undeletedFees")
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
  @Setter(AccessLevel.NONE)
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

  @OneToMany(mappedBy = "fee", cascade = REMOVE, fetch = EAGER)
  @EqualsAndHashCode.Exclude
  private List<Mpbs> mobilePayments;

  @OneToMany(mappedBy = "fee", cascade = REMOVE)
  @Setter(AccessLevel.NONE)
  @JsonIgnore
  @EqualsAndHashCode.Exclude
  private List<FeeStatusHistory> statusHistories;

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

  public Fee updateStatus(FeeStatusEnum newStatus) {
    if (isValidNewStatus(newStatus)) {
      this.status = newStatus;
      return this;
    }
    throw new IllegalArgumentException(
        String.format(
            "New Fee status is not valid" + "\nFee status %s cannot be changed to %s",
            this.status, newStatus));
  }

  private boolean isValidNewStatus(FeeStatusEnum newStatus) {
    return switch (this.status) {
      case PAID -> Stream.of(PENDING, PAID).anyMatch(e -> e.equals(newStatus));
      case UNPAID, PENDING, LATE -> true;
    };
  }

  public Optional<FeeStatusEnum> getStatusAt(Instant instant) {
    return this.statusHistories.stream()
        .filter(fee -> fee.getDatetime().equals(instant) || fee.getDatetime().isBefore(instant))
        .max(comparing(FeeStatusHistory::getDatetime))
        .map(FeeStatusHistory::getStatus);
  }

  public boolean haveNoPendingMobilePayments() {
    return mobilePayments.stream().map(Mpbs::getStatus).noneMatch(isEqual(MpbsStatus.PENDING));
  }

  public boolean mustBeLate() {
    return Instant.now().isAfter(dueDatetime)
        && !PAID.equals(status)
        && (UNPAID.equals(status) || haveNoPendingMobilePayments());
  }
}
