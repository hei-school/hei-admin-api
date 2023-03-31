package school.hei.haapi.model;

import static javax.persistence.GenerationType.IDENTITY;
import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "\"delay_penalty_history\"")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DelayPenaltyHistory {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;
  private int interestPercent;
  private int timeFrequency;
  private Instant startDate;
  private Instant endDate;
  @ManyToOne
  @JoinColumn(name = "delay_penalty_id", nullable = false)
  private DelayPenalty delayPenalty;
}
