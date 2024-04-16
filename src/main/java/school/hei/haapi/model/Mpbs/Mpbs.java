package school.hei.haapi.model.Mpbs;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.User;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "\"mpbs\"")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class Mpbs extends SuperEntity implements Serializable {
    private Integer amount;

    private Instant successfullyVerifiedOn;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private User user;

    @OneToOne
    @JoinColumn(name = "fee_id")
    private Fee fee;
}
