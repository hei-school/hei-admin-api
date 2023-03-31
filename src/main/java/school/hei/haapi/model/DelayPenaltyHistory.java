package school.hei.haapi.model;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

import static javax.persistence.GenerationType.IDENTITY;

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
  private LocalDate startDate;
  private LocalDate endDate;
  private Instant creationDate;
  @ManyToOne
  @JoinColumn(name = "delay_penalty_id", nullable = false)
  private DelayPenalty delayPenalty;
}
