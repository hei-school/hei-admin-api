package school.hei.haapi.model;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "\"mobile_transaction_details\"")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MobileTransactionDetails implements Serializable {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  private Integer pspTransactionAmount;

  private Instant pspDatetimeTransactionCreation;

  private String pspTransactionRef;

  private String studentRef;
}
