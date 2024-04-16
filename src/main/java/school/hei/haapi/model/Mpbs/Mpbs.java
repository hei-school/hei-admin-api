package school.hei.haapi.model.Mpbs;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.User;

@Entity
@Table(name = "\"mpbs\"")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
<<<<<<< HEAD
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
=======
@EqualsAndHashCode
>>>>>>> 12d9e30 (feat: crupdate and get mbps)
public class Mpbs extends SuperEntity implements Serializable {
  private Integer amount;

  private Instant successfullyVerifiedOn;

<<<<<<< HEAD
    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;

    @OneToOne
    @JoinColumn(name = "fee_id")
    private Fee fee;
}
=======
  @ManyToOne
  @JoinColumn(name = "student_id")
  private User student;

  @OneToOne
  @JoinColumn(name = "fee_id")
  private Fee fee;
}
>>>>>>> 12d9e30 (feat: crupdate and get mbps)
