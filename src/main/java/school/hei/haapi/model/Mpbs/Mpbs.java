package school.hei.haapi.model.Mpbs;

import static jakarta.persistence.EnumType.STRING;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
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
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString
public class Mpbs extends TypedMobileMoneyTransaction implements Serializable {
  private Integer amount;

  private Instant successfullyVerifiedOn;

  private Instant lastVerificationDatetime;

  private Instant pspOwnDatetimeVerification;

  @ManyToOne
  @JoinColumn(name = "student_id")
  private User student;

  @OneToOne
  @JoinColumn(name = "fee_id")
  private Fee fee;

  @Column(name = "\"status\"")
  @Enumerated(STRING)
  @JdbcTypeCode(NAMED_ENUM)
  private MpbsStatus status;
}
