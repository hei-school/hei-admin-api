package school.hei.haapi.model.Mpbs;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.User;

@Entity
@Table(name = "mpbs_verification")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MpbsVerification extends TypedMobileMoneyTransaction implements Serializable {
  private Integer amountOfFeeRemainingPayment;

  private Integer amountInPsp;

  private Instant creationDatetimeOfPaymentInPsp;

  private Instant creationDatetimeOfMpbs;

  private String comment;

  @ManyToOne
  @JoinColumn(name = "fee_id")
  private Fee fee;

  @ManyToOne
  @JoinColumn(name = "student_id")
  private User student;
}
