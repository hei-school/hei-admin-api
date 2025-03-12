package school.hei.haapi.model;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "\"payment_number_sequence\"")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
public class PaymentNumberSequence {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  private String yearMonth;
  private int sequenceNumber;

  public String getStringSequence() {
    return yearMonth + "-" + String.format("%04d", sequenceNumber);
  }
}
