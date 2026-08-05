package school.hei.haapi.model;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.io.Serializable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;

@Entity
@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
public class CreditTransaction implements Serializable {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @ManyToOne
  @JoinColumn(name = "credit_id", nullable = false, updatable = false)
  private Credit credit;

  @JdbcTypeCode(NAMED_ENUM)
  @Enumerated(STRING)
  private CreditMovement creditMovement;

  @ManyToOne
  @JoinColumn(name = "fee_id", nullable = false, updatable = false)
  private Fee fee;

  private int amount;

  private Instant creationDatetime;
}
