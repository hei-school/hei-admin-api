package school.hei.haapi.model.Mpbs;

import jakarta.persistence.Entity;
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
public class Mpbs extends TypedMobileMoneyTransaction implements Serializable {
  private Integer amount;

  private Instant successfullyVerifiedOn;

  @ManyToOne
  @JoinColumn(name = "student_id")
  private User student;

  @OneToOne
  @JoinColumn(name = "fee_id")
  private Fee fee;
}
