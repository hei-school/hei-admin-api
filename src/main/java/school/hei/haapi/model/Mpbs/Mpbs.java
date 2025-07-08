package school.hei.haapi.model.Mpbs;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.EAGER;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import school.hei.haapi.endpoint.rest.model.MpbsStatus;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.User;

@Entity
@Table(name = "\"mpbs\"")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class Mpbs extends TypedMobileMoneyTransaction implements Serializable {
  private Integer amount;

  private Instant successfullyVerifiedOn;

  private Instant lastVerificationDatetime;

  private Instant pspOwnDatetimeVerification;

  @ManyToOne
  @JoinColumn(name = "student_id")
  @ToString.Exclude
  private User student;

  @ManyToOne
  @JoinColumn(name = "fee_id")
  @ToString.Exclude
  @JsonIgnore
  private Fee fee;

  @Column(name = "\"status\"")
  @Enumerated(STRING)
  @JdbcTypeCode(NAMED_ENUM)
  private MpbsStatus status;

  @OneToMany(mappedBy = "mpbs", cascade = ALL, fetch = EAGER)
  @JsonIgnore
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  private List<MpbsStatusHistory> statusHistory;

  public boolean exceedsValidationDate() {
    return getCreationDatetime().until(now(), DAYS) > 2;
  }
}
